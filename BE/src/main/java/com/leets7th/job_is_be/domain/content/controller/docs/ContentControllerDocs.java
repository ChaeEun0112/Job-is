package com.leets7th.job_is_be.domain.content.controller.docs;

import com.leets7th.job_is_be.domain.content.dto.ContentDetailResponse;
import com.leets7th.job_is_be.domain.content.dto.ContentSummaryResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Content", description = "오늘의 소식(뉴스/혜택) 목록/상세 조회 API")
@SecurityRequirement(name = "JWT")
public interface ContentControllerDocs {

    @Operation(
            summary = "오늘의 소식 목록 조회",
            description = "뉴스/혜택 콘텐츠 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<ContentSummaryResponse>>> getContents();

    @Operation(
            summary = "오늘의 소식 상세 조회",
            description = "콘텐츠 ID로 뉴스/혜택 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ContentDetailResponse>> getContent(
            @Parameter(description = "콘텐츠 ID") Long contentId
    );
}
