package com.leets7th.job_is_be.domain.notification.controller;

import com.leets7th.job_is_be.domain.notification.controller.docs.UnsubscribeControllerDocs;
import com.leets7th.job_is_be.domain.notification.dto.UnsubscribeFeedbackRequest;
import com.leets7th.job_is_be.domain.notification.dto.UnsubscribeResponse;
import com.leets7th.job_is_be.domain.notification.service.UnsubscribeService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 토큰 기반 수신거부 API. 로그인 없이 접근 가능 (SecurityConfig에서 permitAll).
 */
@RestController
@RequestMapping("/api/unsubscribe/{token}")
@RequiredArgsConstructor
public class UnsubscribeController implements UnsubscribeControllerDocs {

    private final UnsubscribeService unsubscribeService;

    @GetMapping
    public ResponseEntity<ApiResponse<UnsubscribeResponse>> unsubscribe(
            @PathVariable String token
    ) {
        return ApiResponse.success(
                SuccessStatus.UNSUBSCRIBE_SUCCESS,
                unsubscribeService.unsubscribe(token)
        );
    }

    @PostMapping("/resubscribe")
    public ResponseEntity<ApiResponse<UnsubscribeResponse>> resubscribe(
            @PathVariable String token
    ) {
        return ApiResponse.success(
                SuccessStatus.RESUBSCRIBE_SUCCESS,
                unsubscribeService.resubscribe(token)
        );
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<Void>> submitFeedback(
            @PathVariable String token,
            @Valid @RequestBody UnsubscribeFeedbackRequest request
    ) {
        unsubscribeService.submitFeedback(token, request);
        return ApiResponse.success(SuccessStatus.UNSUBSCRIBE_FEEDBACK_SUCCESS);
    }
}
