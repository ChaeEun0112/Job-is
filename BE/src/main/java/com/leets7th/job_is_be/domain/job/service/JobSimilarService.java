package com.leets7th.job_is_be.domain.job.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets7th.job_is_be.domain.job.dto.CriteriaMatrixDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobItemDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobsResponseDto;
import com.leets7th.job_is_be.domain.job.enums.FitCriteriaStatus;
import com.leets7th.job_is_be.domain.personality.entity.PersonalityTest;
import com.leets7th.job_is_be.domain.personality.repository.PersonalityTestRepository;
import com.leets7th.job_is_be.global.ai.OpenAiProperties;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSimilarService {

    // 임베딩(120s) + LLM 선별(90s) 합산 여유값
    private static final long PYTHON_TIMEOUT_SECONDS = 300;

    // 본문 임베딩 유사도가 이 값 이상이면 관심 직무와 유사하다고 본다
    private static final double COSINE_SIMILAR_THRESHOLD = 0.6;

    private final PersonalityTestRepository personalityTestRepository;
    private final OpenAiProperties openAiProperties;
    private final TransactionTemplate transactionTemplate;

    @org.springframework.beans.factory.annotation.Value("${crawler.python-path:python}")
    private String pythonPath;

    @org.springframework.beans.factory.annotation.Value("${matching.database-url:}")
    private String databaseUrl;

    /**
     * 사용자의 성향 퀴즈 결과를 기반으로 맞춤 공고를 추천합니다.
     *
     * <p>호출 시점에 트랜잭션이 없어야 합니다(커넥션 풀 고갈 방지).
     * 성향 조회(짧은 읽기 TX) → Python 실행(트랜잭션 없음) 순서로 실행됩니다.
     */
    public SimilarJobsResponseDto getRecommendedJobsByPersonality(Long userId) {
        // 짧은 읽기 TX: 성향 결과만 로드하고 바로 커밋
        String persona = transactionTemplate.execute(status -> {
            PersonalityTest personality = personalityTestRepository
                    .findFirstByUserIdAndCompletedTrueOrderByStartedAtDesc(userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.PERSONALITY_NOT_FOUND));
            return personality.getResultType() != null
                    ? personality.getResultType().name().toLowerCase()
                    : "p2-senior-backend";
        });

        // Python 실행 — 트랜잭션 없음
        List<SimilarJobItemDto> items = executePythonRetrieve(persona);
        return SimilarJobsResponseDto.of(items);
    }

    private List<SimilarJobItemDto> executePythonRetrieve(String persona) {

        java.io.File stderrFile = null;
        java.io.File stdoutFile = null;
        try {
            java.io.File workDir = new java.io.File(System.getProperty("user.dir"));
            java.io.File script = new java.io.File(workDir, "database/matching/engine/retrieve_user_json.py");
            stderrFile = java.io.File.createTempFile("retrieve-stderr-", ".log");
            stdoutFile = java.io.File.createTempFile("retrieve-stdout-", ".json");

            // 동적 페르소나를 인자로 파이썬 스크립트 실행
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    script.getAbsolutePath(),
                    "--persona",
                    persona
            );

            pb.directory(workDir);
            // stdout/stderr 를 모두 파일로 받는다.
            // 파이프로 받으면서 waitFor 로 먼저 대기하면, 출력이 파이프 버퍼(윈도우 기본 4KB)를 넘는 순간
            // 파이썬은 "자바가 읽어가길", 자바는 "파이썬이 끝나길" 기다리는 교착이 발생한다.
            pb.redirectOutput(stdoutFile);
            pb.redirectError(stderrFile);

            pb.environment().put("DATABASE_URL", databaseUrl);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            // fastembed(ONNX Runtime)가 t3.small(2 vCPU)보다 많은 스레드를 잡으려다
            // 스레드 오버서브스크립션으로 300건 임베딩이 11분 넘게 멈추는 문제가 있었음(2026-08-06).
            // 스레드 수를 1로 제한하니 300건이 1분 22초로 끝남.
            pb.environment().put("OMP_NUM_THREADS", "1");
            pb.environment().put("ORT_INTRA_OP_NUM_THREADS", "1");
            pb.environment().put("ORT_INTER_OP_NUM_THREADS", "1");
            if (openAiProperties.apiKey() != null && !openAiProperties.apiKey().isBlank()) {
                pb.environment().put("OPENAI_API_KEY", openAiProperties.apiKey());
                pb.environment().put("OPENAI_MODEL", openAiProperties.model());
            }

            Process process = pb.start();

            // 임베딩 모델 최초 로딩이 오래 걸릴 수 있어 여유를 둔다
            boolean completed = process.waitFor(PYTHON_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                log.error("[Python Retrieve] {}초 안에 끝나지 않아 강제 종료했습니다. persona={}",
                        PYTHON_TIMEOUT_SECONDS, persona);
                throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
            }

            int exitCode = process.exitValue();

            StringBuilder output = new StringBuilder(readFileQuietly(stdoutFile));
            String errorOutput = readFileQuietly(stderrFile);
            log.info("[Python Retrieve] persona={}, exitCode={}, stdout_len={}, stderr_len={}",
                    persona, exitCode, output.length(), errorOutput.length());

            if (!errorOutput.isBlank()) {
                log.info("[Python Retrieve] stderr:\n{}", errorOutput);
            }

            if (output.length() == 0) {
                // 스크립트가 stdout 에 아무것도 남기지 못하고 죽은 경우 — 원인은 stderr 에만 있다
                log.error("[Python Retrieve] 파이썬이 출력을 내지 못했습니다. persona={}, exitCode={}, python={}, script={}\n--- stderr ---\n{}",
                        persona, exitCode, pythonPath, script.getAbsolutePath(),
                        errorOutput.isBlank() ? "(stderr 없음 — 실행 파일 경로를 확인하세요)" : errorOutput);
                throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
            }

            log.debug("[Python Retrieve] stdout: {}", output.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(output.toString());
            JsonNode candidatesNode = rootNode.get("candidates");

            JsonNode personaNode = rootNode.get("persona");

            List<SimilarJobItemDto> items = new ArrayList<>();
            if (candidatesNode != null && candidatesNode.isArray()) {
                for (JsonNode node : candidatesNode) {
                    // score_final 최대값은 1.2(코사인 1.0 + 스킬 0.15 + 규모 0.05).
                    // 최솟값 65 보장(A) + sqrt 비선형 스케일링으로 낮은 점수 구간을 추가 보정(B).
                    double scoreFinal = node.get("score_final").asDouble();
                    int fitScore = (int) Math.max(65, Math.min(100, Math.round(65 + Math.sqrt(scoreFinal / 1.2) * 35)));

                    // LLM이 생성한 fit_points 우선, 없으면 Java 규칙 기반
                    List<String> fitPoints;
                    JsonNode llmFitPoints = node.get("fit_points");
                    if (llmFitPoints != null && llmFitPoints.isArray() && llmFitPoints.size() > 0) {
                        fitPoints = new ArrayList<>();
                        llmFitPoints.forEach(fp -> fitPoints.add(fp.asText()));
                    } else {
                        fitPoints = buildFitPoints(node, personaNode);
                    }

                    // LLM이 생성한 reason 우선, 없으면 fitPoints 첫 항목
                    JsonNode llmReasonNode = node.get("reason");
                    String reason = (llmReasonNode != null && !llmReasonNode.isNull()
                            && !llmReasonNode.asText().isBlank())
                            ? llmReasonNode.asText()
                            : buildReason(fitPoints);

                    items.add(new SimilarJobItemDto(
                            node.get("external_id").asText(),
                            node.get("position").asText(),
                            node.get("company").asText(),
                            fitScore,
                            reason,
                            fitPoints,
                            buildCriteriaMatrix(node, personaNode)
                    ));
                }
            }
            log.info("[Python Retrieve] Found {} candidates for persona: {}", items.size(), persona);
            return items;
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Python Retrieve] 실행/파싱 실패 persona={}", persona, e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // 예외가 나도 임시 파일은 반드시 정리한다
            deleteQuietly(stdoutFile);
            deleteQuietly(stderrFile);
        }
    }

    /**
     * 확인 가능한 신호만 근거로 남긴다. 파이썬 출력에 없는 축(직무·경력)은 주장하지 않는다.
     */
    private List<String> buildFitPoints(JsonNode candidate, JsonNode persona) {
        List<String> points = new ArrayList<>();

        List<String> matchedSkills = matchedSkills(candidate, persona);
        if (!matchedSkills.isEmpty()) {
            points.add(String.join(", ", matchedSkills) + " 스킬이 겹칩니다");
        }
        if (isLocationMatched(candidate, persona)) {
            points.add("선호 지역과 일치합니다");
        }
        if (isCompanySizeMatched(candidate, persona)) {
            points.add(text(candidate, "company_type") + " 규모를 선호합니다");
        }

        double cosine = candidate.path("score_cosine").asDouble();
        if (cosine >= COSINE_SIMILAR_THRESHOLD) {
            points.add("공고 내용이 관심 직무와 유사합니다");
        }
        return points;
    }

    private String buildReason(List<String> fitPoints) {
        if (fitPoints.isEmpty()) {
            return "관심 직무와 유사한 공고입니다";
        }
        return String.join(" · ", fitPoints);
    }

    /**
     * 확인 가능한 축만 판정한다.
     *
     * <p>판정값의 의미는 {@link FitCriteriaStatus} 정의를 따른다. 비교할 데이터 자체가 없으면
     * {@code UNKNOWN}, 비교했는데 어긋나면 {@code CAUTION} 이며, 근거가 없다는 이유로
     * {@code ESTIMATED} 를 쓰지 않는다(공고 상세 매칭과 같은 값이 같은 의미를 갖도록).
     *
     * <p>급여는 데이터가 없어 항상 {@code CAUTION}(화면설계서 §2.3).
     */
    private CriteriaMatrixDto buildCriteriaMatrix(JsonNode candidate, JsonNode persona) {
        FitCriteriaStatus skills = judge(
                textList(persona, "skills").isEmpty() || textList(candidate, "skill_tags").isEmpty(),
                !matchedSkills(candidate, persona).isEmpty());

        FitCriteriaStatus location = judge(
                textList(persona, "locations").isEmpty() || text(candidate, "location_full").isBlank(),
                isLocationMatched(candidate, persona));

        FitCriteriaStatus preference = judge(
                textList(persona, "company_size_pref").isEmpty() || text(candidate, "company_type").isBlank(),
                isCompanySizeMatched(candidate, persona));

        // 직무는 세부직군 교집합이 아니라 본문 임베딩 유사도로만 추정한다 — 하드 매칭으로 주장하지 않는다.
        FitCriteriaStatus jobType;
        if (!candidate.hasNonNull("score_cosine")) {
            jobType = FitCriteriaStatus.UNKNOWN;
        } else {
            jobType = candidate.path("score_cosine").asDouble() >= COSINE_SIMILAR_THRESHOLD
                    ? FitCriteriaStatus.ESTIMATED
                    : FitCriteriaStatus.CAUTION;
        }

        // 경력은 파이썬 응답에 담기지 않아 판정 불가
        return new CriteriaMatrixDto(
                jobType, FitCriteriaStatus.UNKNOWN, location, skills, preference, FitCriteriaStatus.CAUTION);
    }

    /** 비교 대상이 없으면 UNKNOWN, 비교해서 맞으면 MATCH, 어긋나면 CAUTION. */
    private FitCriteriaStatus judge(boolean noEvidence, boolean matched) {
        if (noEvidence) {
            return FitCriteriaStatus.UNKNOWN;
        }
        return matched ? FitCriteriaStatus.MATCH : FitCriteriaStatus.CAUTION;
    }

    private List<String> matchedSkills(JsonNode candidate, JsonNode persona) {
        List<String> personaSkills = textList(persona, "skills");
        if (personaSkills.isEmpty()) {
            return List.of();
        }
        return textList(candidate, "skill_tags").stream()
                .filter(tag -> personaSkills.stream().anyMatch(tag::equalsIgnoreCase))
                .toList();
    }

    private boolean isLocationMatched(JsonNode candidate, JsonNode persona) {
        String location = text(candidate, "location_full");
        if (location.isBlank()) {
            return false;
        }
        return textList(persona, "locations").stream().anyMatch(location::contains);
    }

    private boolean isCompanySizeMatched(JsonNode candidate, JsonNode persona) {
        String companyType = text(candidate, "company_type");
        if (companyType.isBlank()) {
            return false;
        }
        return textList(persona, "company_size_pref").contains(companyType);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private List<String> textList(JsonNode node, String field) {
        JsonNode array = node == null ? null : node.get(field);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        array.forEach(element -> values.add(element.asText()));
        return values;
    }

    private String readFileQuietly(java.io.File file) {
        try {
            return java.nio.file.Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private void deleteQuietly(java.io.File file) {
        if (file != null && file.exists() && !file.delete()) {
            log.warn("[Python Retrieve] 임시 파일 삭제 실패: {}", file.getAbsolutePath());
        }
    }
}
