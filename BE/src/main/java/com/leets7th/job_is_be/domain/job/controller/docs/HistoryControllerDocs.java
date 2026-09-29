package com.leets7th.job_is_be.domain.job.controller.docs;

import com.leets7th.job_is_be.domain.job.dto.HistoryItemResponse;
import com.leets7th.job_is_be.domain.job.enums.HistoryFilterType;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "History", description = "공고 상호작용 히스토리 조회 API")
@SecurityRequirement(name = "JWT")
public interface HistoryControllerDocs {

    @Operation(
            summary = "히스토리 조회",
            description = "로그인한 사용자의 공고 열람/지원 등 상호작용 이력을 필터 조건에 따라 페이징 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<PageResponse<HistoryItemResponse>>> getHistory(
            @Parameter(hidden = true) Jwt jwt,
            @Parameter(description = "페이지 번호 (1부터 시작, 기본값: 1)", example = "1") int page,
            @Parameter(description = "페이지 크기 (기본값: 24)", example = "24") int size,
            @Parameter(description = "히스토리 필터 유형 (기본값: ALL)", example = "ALL") HistoryFilterType filter
    );
}
