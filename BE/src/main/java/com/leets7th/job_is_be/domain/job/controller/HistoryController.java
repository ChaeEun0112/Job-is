package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.controller.docs.HistoryControllerDocs;
import com.leets7th.job_is_be.domain.job.dto.HistoryItemResponse;
import com.leets7th.job_is_be.domain.job.enums.HistoryFilterType;
import com.leets7th.job_is_be.domain.job.service.HistoryService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.response.PageResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController implements HistoryControllerDocs {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<HistoryItemResponse>>> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(defaultValue = "ALL") HistoryFilterType filter
    ) {
        PageResponse<HistoryItemResponse> response =
                historyService.getHistory(Long.valueOf(jwt.getSubject()), page, size, filter);
        return ApiResponse.success(SuccessStatus.HISTORY_LIST_GET_SUCCESS, response);
    }
}
