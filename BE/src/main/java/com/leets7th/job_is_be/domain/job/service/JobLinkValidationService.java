package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.JobLinkValidateResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;
import java.net.URI;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobLinkValidationService {

    private final RestClient restClient;
    private final JobRepository jobRepository;

    /**
     * 공고 원문 링크 전체 유효성 검증 (사전 검증 + 네트워크 통신)
     */
    public JobLinkValidateResponse validateJobLink(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.JOB_NOT_FOUND));

        // 사전 검증 (공고 상태, URL 존재 여부, 형식 및 SSRF)
        Optional<JobLinkValidateResponse> preconditionError = checkPreconditions(job);
        if (preconditionError.isPresent()) {
            return preconditionError.get();
        }

        // 실제 네트워크 연결 검증 (HEAD -> GET Fallback)
        return validateLink(job.getSourceUrl());
    }

    /**
     * 1차 검증, 공고 상태 및 URL 유효성/보안 사전 체크
     */
    private Optional<JobLinkValidateResponse> checkPreconditions(Job job) {
        // 공고 상태 검증
        if (job.getStatus() != JobStatus.ACTIVE) {
            return Optional.of(JobLinkValidateResponse.of(false, null, ErrorStatus.JOB_NOT_APPLICABLE.getMessage()));
        }

        String url = job.getSourceUrl();

        // URL 존재 여부 검증
        if (url == null || url.isBlank()) {
            return Optional.of(JobLinkValidateResponse.of(false, null, ErrorStatus.JOB_LINK_EMPTY.getMessage()));
        }

        // URL 프로토콜 및 SSRF 보안 검증
        if (isInvalidUrlFormatOrSsrf(url)) {
            return Optional.of(JobLinkValidateResponse.of(false, null, ErrorStatus.JOB_LINK_INVALID_FORMAT.getMessage()));
        }

        return Optional.empty();
    }

    /**
     * URL 프로토콜(http/https) 및 SSRF(사설망/내부망) 검증
     */
    private boolean isInvalidUrlFormatOrSsrf(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return true;
        }
        return isPrivateOrLocalUrl(url);
    }

    /**
     * 2차 검증, 네트워크 연결 테스트 (HEAD 시도 후 실패 시 GET 재시도)
     */
    public JobLinkValidateResponse validateLink(String url) {
        // 1차: HEAD 요청
        JobLinkValidateResponse headResult = executeRequest(url, HttpMethod.HEAD);
        if (headResult.isValid()) {
            return headResult;
        }

        // 2차: GET 요청 Fallback (보안을 위해 Host만 기록)
        log.info("HEAD 요청 실패로 GET 요청 재시도를 진행합니다. Host: {}", extractHost(url));
        return executeRequest(url, HttpMethod.GET);
    }

    /**
     * HTTP 요청 실행 및 상태 코드/예외 응답 캡슐화
     */
    private JobLinkValidateResponse executeRequest(String url, HttpMethod method) {
        try {
            HttpStatusCode statusCode = restClient.method(method)
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode();

            boolean isValid = statusCode.is2xxSuccessful() || statusCode.is3xxRedirection();
            String message = isValid
                    ? "정상 접근 가능한 링크입니다."
                    : ErrorStatus.JOB_LINK_UNREACHABLE.getMessage() + " (상태 코드: " + statusCode.value() + ")";

            return JobLinkValidateResponse.of(isValid, statusCode.value(), message);

        } catch (HttpStatusCodeException e) {
            // 4xx, 5xx 에러 처리
            int code = e.getStatusCode().value();
            return JobLinkValidateResponse.of(false, code, ErrorStatus.JOB_LINK_UNREACHABLE.getMessage() + " (상태 코드: " + code + ")");
        } catch (Exception e) {
            // 연결/타임아웃 에러 처리 (보안을 위해 Host만 기록)
            log.warn("링크 유효성 검증 실패 (Method: {}, Host: {}): {}", method, extractHost(url), e.getMessage());
            return JobLinkValidateResponse.of(false, null, ErrorStatus.JOB_LINK_CONNECT_FAILED.getMessage());
        }
    }

    /**
     * [SSRF 방지] Localhost, 메타데이터 IP, 사설 IP 대역 접근 차단
     */
    private boolean isPrivateOrLocalUrl(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            String host = uri.getHost();
            if (host == null) return true;

            // Localhost 및 주요 클라우드 메타데이터 IP 차단
            if (host.equalsIgnoreCase("localhost") || host.equals("169.254.169.254")) {
                return true;
            }

            // 사설/루프백/링크로컬 IP 차단 (다중 A/AAAA 레코드 전체 검증)
            InetAddress[] inetAddresses = InetAddress.getAllByName(host);
            for (InetAddress inetAddress : inetAddresses) {
                if (inetAddress.isLoopbackAddress()
                        || inetAddress.isSiteLocalAddress()
                        || inetAddress.isLinkLocalAddress()
                        || inetAddress.isAnyLocalAddress()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return true; // Parsing failure 등 이상 URL은 차단
        }
    }

    /**
     * [보안] 로그 기록 시 민감 정보(토큰, 쿼리 파라미터) 유출 방지용 Host 추출 메서드
     */
    private String extractHost(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            return uri.getHost() != null ? uri.getHost() : "unknown-host";
        } catch (Exception e) {
            return "invalid-url";
        }
    }
}
