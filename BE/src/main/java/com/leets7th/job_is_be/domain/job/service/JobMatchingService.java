package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.job.dto.CriteriaMatrixDto;
import com.leets7th.job_is_be.domain.job.dto.JobMatchingResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.FitCriteriaStatus;
import com.leets7th.job_is_be.domain.user.entity.UserJobCategory;
import com.leets7th.job_is_be.domain.user.entity.UserProfile;
import com.leets7th.job_is_be.domain.user.entity.UserRegion;
import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import com.leets7th.job_is_be.domain.user.repository.UserJobCategoryRepository;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRegionRepository;
import com.leets7th.job_is_be.domain.user.service.UserTechStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 공고 상세(DET-01 ④⑥)에 실을 사용자 매칭 정보를 산출한다.
 *
 * <p>덱에 담긴 적 있는 공고는 덱 확정 시점에 저장한 점수를 그대로 쓰고,
 * 검색·저장 목록으로 들어온 공고는 조회 시점에 산출한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobMatchingService {

    /** 축 하나가 매칭됐을 때의 가중치. 급여 축은 항상 CAUTION 이라 점수 산출에서 제외한다. */
    private static final int SCORED_AXIS_COUNT = 5;

    private final CardRepository cardRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserJobCategoryRepository userJobCategoryRepository;
    private final UserRegionRepository userRegionRepository;
    private final UserTechStackService userTechStackService;

    /**
     * @return 매칭 정보. 성향 퀴즈를 완료하지 않은 사용자면 null
     */
    public JobMatchingResponse resolve(Long userId, Job job) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null || !profile.isJobTestCompleted()) {
            return null;
        }

        CriteriaMatrixDto matrix = buildMatrix(userId, profile, job);
        List<String> reasons = buildReasons(userId, profile, job, matrix);

        Integer storedScore = findStoredScore(userId, job.getId());
        int score = storedScore != null ? storedScore : estimateScore(matrix);

        return JobMatchingResponse.of(score, reasons, matrix);
    }

    /** 덱 확정 시점 점수. 없으면 null. */
    private Integer findStoredScore(Long userId, Long jobId) {
        List<Card> cards = cardRepository.findByUserIdAndJobId(userId, jobId);
        for (Card card : cards) {
            BigDecimal fitScore = card.getFitScore();
            if (fitScore != null) {
                return fitScore.intValue();
            }
        }
        return null;
    }

    private CriteriaMatrixDto buildMatrix(Long userId, UserProfile profile, Job job) {
        return new CriteriaMatrixDto(
                judgeJobType(userId, job),
                judgeCareer(profile, job),
                judgeLocation(userId, job),
                judgeSkills(userId, job),
                judgePreference(job),
                // 급여는 DB 미보유 — 설계서 불변 규칙상 항상 주의
                FitCriteriaStatus.CAUTION
        );
    }

    /** 관심 직무명이 공고의 세부직군과 겹치면 충족, 공고 제목에서만 발견되면 추정. */
    private FitCriteriaStatus judgeJobType(Long userId, Job job) {
        List<String> interests = userJobCategoryRepository.findAllByUserId(userId).stream()
                .map(UserJobCategory::getJobCategory)
                .filter(category -> category != null)
                .map(category -> category.getName())
                .filter(name -> name != null && !name.isBlank())
                .toList();
        if (interests.isEmpty()) {
            return FitCriteriaStatus.UNKNOWN;
        }

        List<String> categoryChild = job.getCategoryChild() == null ? List.of() : job.getCategoryChild();
        boolean hardMatch = categoryChild.stream()
                .anyMatch(child -> interests.stream().anyMatch(interest -> equalsIgnoreCase(child, interest)));
        if (hardMatch) {
            return FitCriteriaStatus.MATCH;
        }

        String title = job.getTitle() == null ? "" : job.getTitle().toLowerCase(Locale.ROOT);
        boolean titleMatch = interests.stream().anyMatch(interest -> title.contains(interest.toLowerCase(Locale.ROOT)));
        return titleMatch ? FitCriteriaStatus.ESTIMATED : FitCriteriaStatus.CAUTION;
    }

    /**
     * 사용자 경력 단계를 연차 구간으로 환산해 공고 요구 구간과 겹치는지 본다.
     * 겹치면 충족, 겹치지 않으면(요건 초과·미달 모두) 주의, 공고에 경력 정보가 없으면 판정 불가.
     */
    private FitCriteriaStatus judgeCareer(UserProfile profile, Job job) {
        CareerLevel careerLevel = profile.getCareerLevel();
        if (careerLevel == null) {
            return FitCriteriaStatus.UNKNOWN;
        }

        boolean newbieWelcome = Boolean.TRUE.equals(job.getIsNewbie())
                || (job.getCareerMin() != null && job.getCareerMin() == 0);
        if (careerLevel == CareerLevel.ENTRY) {
            if (newbieWelcome) {
                return FitCriteriaStatus.MATCH;
            }
            return job.getCareerMin() != null && job.getCareerMin() > 0
                    ? FitCriteriaStatus.CAUTION
                    : FitCriteriaStatus.UNKNOWN;
        }

        if (job.getCareerMin() == null && job.getCareerMax() == null) {
            // 경력무관이거나 원문에 연차 표기가 없는 공고 — 근거 없이 충족이라고 하지 않는다
            return FitCriteriaStatus.UNKNOWN;
        }

        int jobMin = job.getCareerMin() != null ? job.getCareerMin() : 0;
        int jobMax = job.getCareerMax() != null ? job.getCareerMax() : Integer.MAX_VALUE;
        int userMin = minYears(careerLevel);
        int userMax = maxYears(careerLevel);

        boolean overlapped = userMax >= jobMin && userMin <= jobMax;
        return overlapped ? FitCriteriaStatus.MATCH : FitCriteriaStatus.CAUTION;
    }

    /** 프로필에는 연차가 없고 단계만 있어 구간으로 환산한다(설계서 신입 / 1~3년 / 3년+ 기준). */
    private int minYears(CareerLevel careerLevel) {
        return switch (careerLevel) {
            case ENTRY -> 0;
            case JUNIOR -> 1;
            case EXPERIENCED -> 4;
        };
    }

    private int maxYears(CareerLevel careerLevel) {
        return switch (careerLevel) {
            case ENTRY -> 0;
            case JUNIOR -> 3;
            case EXPERIENCED -> Integer.MAX_VALUE;
        };
    }

    /** 원격 가능이거나 희망 지역이 공고 지역에 포함되면 충족. */
    private FitCriteriaStatus judgeLocation(Long userId, Job job) {
        if (Boolean.TRUE.equals(job.getRemoteAvailable())) {
            return FitCriteriaStatus.MATCH;
        }
        UserRegion userRegion = userRegionRepository.findFirstByUserIdOrderByIdDesc(userId).orElse(null);
        if (userRegion == null || userRegion.getRegion() == null) {
            return FitCriteriaStatus.UNKNOWN;
        }

        String wanted = userRegion.getRegion().getName();
        if (wanted == null || wanted.isBlank()) {
            return FitCriteriaStatus.UNKNOWN;
        }
        if (containsIgnoreCase(job.getLocationCity(), wanted) || containsIgnoreCase(job.getLocationFull(), wanted)) {
            return FitCriteriaStatus.MATCH;
        }
        boolean unknownLocation = isBlank(job.getLocationCity()) && isBlank(job.getLocationFull());
        return unknownLocation ? FitCriteriaStatus.UNKNOWN : FitCriteriaStatus.CAUTION;
    }

    /**
     * 설계서 불변 규칙: {@code skillsInferred=true} 인 태그로 맞춘 건 하드 매칭으로 주장하지 않고
     * 무조건 추정으로 표기한다.
     */
    private FitCriteriaStatus judgeSkills(Long userId, Job job) {
        List<String> mine = userTechStackService.findNames(userId);
        List<String> jobSkills = job.getSkillTags() == null ? List.of() : job.getSkillTags();
        if (mine.isEmpty() || jobSkills.isEmpty()) {
            return FitCriteriaStatus.UNKNOWN;
        }

        boolean matched = jobSkills.stream()
                .anyMatch(tag -> mine.stream().anyMatch(skill -> equalsIgnoreCase(tag, skill)));
        if (!matched) {
            return FitCriteriaStatus.CAUTION;
        }
        return Boolean.TRUE.equals(job.getSkillsInferred())
                ? FitCriteriaStatus.ESTIMATED
                : FitCriteriaStatus.MATCH;
    }

    /**
     * 구조화된 선호 조건으로만 판정한다.
     *
     * <p>프로필의 선호 조건이 자유 텍스트({@code preferenceNote})라 대조할 기준이 없으므로,
     * 원격 가능 여부 외에는 판정하지 않는다. 공고에 고용형태가 적혀 있다는 사실만으로는
     * 사용자 선호와 비교한 것이 아니므로 ESTIMATED 를 주지 않는다.
     */
    private FitCriteriaStatus judgePreference(Job job) {
        return Boolean.TRUE.equals(job.getRemoteAvailable())
                ? FitCriteriaStatus.MATCH
                : FitCriteriaStatus.UNKNOWN;
    }

    /** 덱 확정 점수가 없을 때 쓰는 조회 시점 산출값. 급여 축은 제외한 5축 평균. */
    private int estimateScore(CriteriaMatrixDto matrix) {
        int total = weight(matrix.jobType())
                + weight(matrix.career())
                + weight(matrix.location())
                + weight(matrix.skills())
                + weight(matrix.preference());
        return total / SCORED_AXIS_COUNT;
    }

    private int weight(FitCriteriaStatus status) {
        return switch (status) {
            case MATCH -> 100;
            case ESTIMATED -> 50;
            case UNKNOWN, CAUTION -> 0;
        };
    }

    /** 확인된 근거만 문장으로 남긴다(설계서 §9 추정 금지). */
    private List<String> buildReasons(Long userId, UserProfile profile, Job job, CriteriaMatrixDto matrix) {
        List<String> reasons = new ArrayList<>();

        if (matrix.skills() == FitCriteriaStatus.MATCH || matrix.skills() == FitCriteriaStatus.ESTIMATED) {
            List<String> mine = userTechStackService.findNames(userId);
            List<String> jobSkills = job.getSkillTags() == null ? List.of() : job.getSkillTags();
            List<String> overlapped = jobSkills.stream()
                    .filter(tag -> mine.stream().anyMatch(skill -> equalsIgnoreCase(tag, skill)))
                    .toList();
            if (!overlapped.isEmpty()) {
                String suffix = matrix.skills() == FitCriteriaStatus.ESTIMATED ? " 스킬이 겹칩니다 (추정)" : " 스킬이 겹칩니다";
                reasons.add(String.join(", ", overlapped) + suffix);
            }
        }
        if (matrix.location() == FitCriteriaStatus.MATCH) {
            reasons.add(Boolean.TRUE.equals(job.getRemoteAvailable())
                    ? "원격 근무가 가능합니다"
                    : "희망 지역과 일치합니다");
        }
        if (matrix.jobType() == FitCriteriaStatus.MATCH) {
            reasons.add("관심 직무와 일치합니다");
        }
        if (matrix.career() == FitCriteriaStatus.MATCH && profile.getCareerLevel() == CareerLevel.ENTRY) {
            reasons.add("신입 지원이 가능합니다");
        }
        return reasons;
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
