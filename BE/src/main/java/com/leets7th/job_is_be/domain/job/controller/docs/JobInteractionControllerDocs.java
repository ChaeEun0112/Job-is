package com.leets7th.job_is_be.domain.job.controller.docs;

import com.leets7th.job_is_be.domain.job.dto.ApplyClickRequest;
import com.leets7th.job_is_be.domain.job.dto.JobInteractionResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "JobInteraction", description = "공고 상호작용(열람/지원 의향/지원 클릭) 기록 API")
@SecurityRequirement(name = "JWT")
public interface JobInteractionControllerDocs {

    @Operation(
            summary = "공고 열람 기록",
            description = "덱/카드를 통해 공고를 직접 열람했을 때 열람 이력을 기록합니다. 사용자는 Access Token(JWT)으로 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<JobInteractionResponse>> recordView(
            @Parameter(description = "공고 ID") Long jobId,
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "지원 의향 토글",
            description = "외부 이동 없이 공고에 대한 지원 의향 상태만 전환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토글 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<JobInteractionResponse>> toggleApplyIntent(
            @Parameter(description = "공고 ID") Long jobId,
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "지원하기 클릭 기록",
            description = "원티드 등 외부 지원 페이지로 이동하는 '지원하기' 클릭을 기록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<JobInteractionResponse>> recordApply(
            @Parameter(description = "공고 ID") Long jobId,
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = false, description = "지원 클릭 메타데이터(옵션)")
            ApplyClickRequest request
    );
}
