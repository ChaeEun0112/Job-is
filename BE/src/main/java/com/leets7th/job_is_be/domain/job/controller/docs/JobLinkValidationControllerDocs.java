package com.leets7th.job_is_be.domain.job.controller.docs;

import com.leets7th.job_is_be.domain.job.dto.JobLinkValidateResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Job", description = "공고 탐색/상세 조회 및 저장 API")
@SecurityRequirement(name = "JWT")
public interface JobLinkValidationControllerDocs {

    @Operation(
            summary = "원문 링크 유효성 확인",
            description = "공고 ID를 받아 해당 공고의 원문 링크 접속 가능 여부를 검증합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<JobLinkValidateResponse>> checkJobLink(
            @Parameter(description = "공고 ID") Long jobId
    );
}
