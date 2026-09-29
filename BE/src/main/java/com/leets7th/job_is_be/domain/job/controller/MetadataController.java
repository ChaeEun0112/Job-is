package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.dto.CareerLevelResponse;
import com.leets7th.job_is_be.domain.job.dto.JobCategoryResponse;
import com.leets7th.job_is_be.domain.job.dto.RegionResponse;
import com.leets7th.job_is_be.domain.job.dto.TechStackResponse;
import com.leets7th.job_is_be.domain.job.enums.TechStackType;
import com.leets7th.job_is_be.domain.job.controller.docs.MetadataControllerDocs;
import com.leets7th.job_is_be.domain.job.service.MetadataQueryService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/jobs/filters")
@RequiredArgsConstructor
public class MetadataController implements MetadataControllerDocs {

    private final MetadataQueryService metadataQueryService;

    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<RegionResponse>>> getAllRegions() {
        List<RegionResponse> response = metadataQueryService.getAllRegions();
        return ApiResponse.success(SuccessStatus.REGION_GET_SUCCESS, response);
    }

    @GetMapping("/job-categories")
    public ResponseEntity<ApiResponse<List<JobCategoryResponse>>> getJobCategories(
            @RequestParam(required = false) String query
    ) {
        List<JobCategoryResponse> response = metadataQueryService.getJobCategories(query);
        return ApiResponse.success(SuccessStatus.JOB_CATEGORY_GET_SUCCESS, response);
    }

    @GetMapping("/career-levels")
    public ResponseEntity<ApiResponse<List<CareerLevelResponse>>> getAllCareerLevels() {
        return ApiResponse.success(SuccessStatus.CAREER_LEVEL_GET_SUCCESS, metadataQueryService.getAllCareerLevels());
    }

    @GetMapping("/employment-types")
    public ResponseEntity<ApiResponse<List<String>>> getAllEmploymentTypes() {
        List<String> response = metadataQueryService.getAllEmploymentTypes();
        return ApiResponse.success(SuccessStatus.EMPLOYMENT_TYPE_GET_SUCCESS, response);
    }

    @GetMapping("/tech-stacks")
    public ResponseEntity<ApiResponse<List<TechStackResponse>>> getAllTechStacks() {
        List<TechStackResponse> response = Arrays.stream(TechStackType.values())
                .map(TechStackResponse::from)
                .toList();
        return ApiResponse.success(SuccessStatus.TECH_STACK_GET_SUCCESS, response);
    }
}
