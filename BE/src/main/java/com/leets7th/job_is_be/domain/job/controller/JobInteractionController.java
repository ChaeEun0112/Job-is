package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.controller.docs.JobInteractionControllerDocs;
import com.leets7th.job_is_be.domain.job.dto.ApplyClickRequest;
import com.leets7th.job_is_be.domain.job.dto.JobInteractionResponse;
import com.leets7th.job_is_be.domain.job.service.JobInteractionService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobInteractionController implements JobInteractionControllerDocs {

    private final JobInteractionService jobInteractionService;

    // 공고 열람 기록 — 덱/카드를 직접 열람했을 때
    @PostMapping("/{jobId}/view")
    public ResponseEntity<ApiResponse<JobInteractionResponse>> recordView(
            @PathVariable Long jobId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        JobInteractionResponse response = jobInteractionService.recordView(Long.valueOf(jwt.getSubject()), jobId);
        return ApiResponse.success(SuccessStatus.JOB_VIEW_RECORD_SUCCESS, response);
    }

    // 지원 의향 토글 기록 — 외부 이동 없이 상태만 전환
    @PostMapping("/{jobId}/apply-intent")
    public ResponseEntity<ApiResponse<JobInteractionResponse>> toggleApplyIntent(
            @PathVariable Long jobId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        JobInteractionResponse response = jobInteractionService.toggleApplyIntent(Long.valueOf(jwt.getSubject()), jobId);
        return ApiResponse.success(SuccessStatus.JOB_APPLY_INTENT_TOGGLE_SUCCESS, response);
    }

    // 지원하기 클릭 기록 — 원티드로 이동할 때
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<ApiResponse<JobInteractionResponse>> recordApply(
            @PathVariable Long jobId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) ApplyClickRequest request
    ) {
        JobInteractionResponse response = jobInteractionService.recordApply(Long.valueOf(jwt.getSubject()), jobId, request);
        return ApiResponse.success(SuccessStatus.JOB_APPLY_RECORD_SUCCESS, response);
    }
}
