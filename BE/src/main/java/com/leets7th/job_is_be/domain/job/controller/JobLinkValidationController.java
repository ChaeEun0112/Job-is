package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.controller.docs.JobLinkValidationControllerDocs;
import com.leets7th.job_is_be.domain.job.dto.JobLinkValidateResponse;
import com.leets7th.job_is_be.domain.job.service.JobLinkValidationService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobLinkValidationController implements JobLinkValidationControllerDocs {

    private final JobLinkValidationService jobLinkValidationService;

    @GetMapping("/{jobId}/source-check")
    public ResponseEntity<ApiResponse<JobLinkValidateResponse>> checkJobLink(
            @PathVariable("jobId") Long jobId
    ) {
        JobLinkValidateResponse response = jobLinkValidationService.validateJobLink(jobId);
        return ApiResponse.success(SuccessStatus.JOB_SOURCE_CHECK_SUCCESS, response);
    }
}
