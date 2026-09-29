package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.controller.docs.SaveControllerDocs;
import com.leets7th.job_is_be.domain.job.dto.SavedJobListResponse;
import com.leets7th.job_is_be.domain.job.enums.SavedJobSortType;
import com.leets7th.job_is_be.domain.job.service.JobService;
import com.leets7th.job_is_be.global.response.ApiResponse;
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
@RequestMapping("/api/saves")
@RequiredArgsConstructor
public class SaveController implements SaveControllerDocs {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<SavedJobListResponse>> getSavedJobs(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "SAVED_DESC") SavedJobSortType sort
    ) {
        SavedJobListResponse response = jobService.getSavedJobs(Long.valueOf(jwt.getSubject()), page, size, sort);
        return ApiResponse.success(SuccessStatus.SAVE_LIST_GET_SUCCESS, response);
    }
}
