package com.leets7th.job_is_be.domain.job.controller.docs;

import com.leets7th.job_is_be.domain.job.dto.CareerLevelResponse;
import com.leets7th.job_is_be.domain.job.dto.JobCategoryResponse;
import com.leets7th.job_is_be.domain.job.dto.RegionResponse;
import com.leets7th.job_is_be.domain.job.dto.TechStackResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Metadata", description = "지역, 직무, 경력 수준 카테고리 메타데이터 조회 API")
@SecurityRequirement(name = "JWT")
public interface MetadataControllerDocs {

    @Operation(summary = "지역 목록 조회", description = "DB에 등록된 지역 메타데이터 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<RegionResponse>>> getAllRegions();

    @Operation(
            summary = "직무 카테고리 목록 및 자동완성 조회",
            description = "query를 생략하면 전체 직무 목록을 반환하고, 입력하면 직무명과 연관 키워드로 자동완성 후보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<JobCategoryResponse>>> getJobCategories(
            @Parameter(description = "직무명 또는 연관 검색어", example = "백앤드")
            String query
    );

    @Operation(summary = "경력 수준 목록 조회", description = "시스템에 정의된 경력 수준 메타데이터를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<CareerLevelResponse>>> getAllCareerLevels();

    @Operation(summary = "고용형태 목록 조회",
            description = "탐색 후보 공고에 실제로 존재하는 고용형태 값 목록을 조회합니다. 탐색 필터의 고용형태 옵션으로 사용합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<String>>> getAllEmploymentTypes();

    @Operation(summary = "기술 스택 목록 조회", description = "시스템에 정의된 전체 기술 스택 메타데이터 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<TechStackResponse>>> getAllTechStacks();
}
