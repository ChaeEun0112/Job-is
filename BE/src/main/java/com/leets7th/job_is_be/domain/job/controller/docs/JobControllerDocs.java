package com.leets7th.job_is_be.domain.job.controller.docs;

import com.leets7th.job_is_be.domain.job.dto.JobDetailResponse;
import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Job", description = "공고 탐색/상세 조회 및 저장 API")
@SecurityRequirement(name = "JWT")
public interface JobControllerDocs {

    @Operation(
            summary = "공고 저장",
            description = "로그인한 사용자가 특정 공고를 저장합니다. 사용자는 Access Token(JWT)으로 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<Void>> saveJob(
            @Parameter(description = "공고 ID") Long jobId,
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "공고 저장 취소",
            description = "로그인한 사용자가 저장했던 공고를 저장 해제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<Void>> unsaveJob(
            @Parameter(description = "공고 ID") Long jobId,
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "공고 탐색 및 검색",
            description = "GET /api/jobs/search — 필터 6종(세부직군·지역 시/구·경력·고용형태·원격·상시채용 포함)과 "
                    + "키워드로 공고 목록을 페이징 조회합니다. "
                    + "후보는 진행 중(ACTIVE·미마감 또는 상시채용)인 공고로 한정됩니다. "
                    + "페이지 크기는 24 고정이며, 정렬은 sort 파라미터(FIT 기본 / RECENT / DEADLINE)로 지정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<PageResponse<JobSummaryResponse>>> searchJobs(
            JobSearchRequest condition,
            Pageable pageable,
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "공고 상세 조회",
            description = """
                    공고 ID로 상세 정보를 조회합니다.
                    로그인 사용자면 matching(적합도 점수·근거·체크리스트)이 함께 내려갑니다.
                    비로그인이거나 성향 퀴즈를 완료하지 않은 사용자는 matching 이 null 입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<JobDetailResponse>> getJobDetail(
            @Parameter(description = "공고 ID") Long jobId,
            org.springframework.security.oauth2.jwt.Jwt jwt
    );
}
