package com.leets7th.job_is_be.domain.job.controller.docs;

import com.leets7th.job_is_be.domain.job.dto.SavedJobListResponse;
import com.leets7th.job_is_be.domain.job.enums.SavedJobSortType;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Save", description = "저장한 공고 목록 조회 API")
@SecurityRequirement(name = "JWT")
public interface SaveControllerDocs {

    @Operation(
            summary = "저장한 공고 목록 조회",
            description = "로그인한 사용자가 저장한 공고 목록을 페이징/정렬 조건에 따라 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<SavedJobListResponse>> getSavedJobs(
            @Parameter(hidden = true) Jwt jwt,
            @Parameter(description = "페이지 번호 (1부터 시작, 기본값: 1)", example = "1") int page,
            @Parameter(description = "페이지 크기 (기본값: 20)", example = "20") int size,
            @Parameter(description = "정렬 기준 (기본값: SAVED_DESC)", example = "SAVED_DESC") SavedJobSortType sort
    );
}
