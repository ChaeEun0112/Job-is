package com.leets7th.job_is_be.domain.job.controller.docs;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.job.dto.SyncJobExecution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Tag(name = "JobCrawler(Admin)", description = "공고 수집 파이프라인 수동 실행 관리자 API")
@SecurityRequirement(name = "JWT")
public interface JobCrawlerControllerDocs {

    @Operation(
            summary = "크롤링 파이프라인 수동 실행",
            description = "전체 공고 수집 및 DB 적재 파이프라인을 즉시 동기 실행합니다. ADMIN 권한이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실행 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    ResponseEntity<com.leets7th.job_is_be.global.response.ApiResponse<String>> triggerCrawlerPipeline();

    @Operation(
            summary = "job_postings → jobs 비동기 동기화 시작",
            description = "동기화 작업을 백그라운드로 시작하고 즉시 executionId를 반환합니다. " +
                    "실제 upsert는 별도 스레드에서 진행되며 GET /sync/{executionId}로 진행 상태를 확인할 수 있습니다. " +
                    "ADMIN 권한이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "동기화 작업 시작됨"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 필요"),
            @ApiResponse(responseCode = "409", description = "동기화 작업이 이미 실행 중")
    })
    ResponseEntity<com.leets7th.job_is_be.global.response.ApiResponse<SyncJobExecution>> syncJobPostings();

    @Operation(
            summary = "동기화 작업 상태 조회",
            description = "POST /sync 로 시작한 작업의 현재 상태를 반환합니다. " +
                    "status: ACCEPTED → RUNNING → COMPLETED | FAILED | TIMED_OUT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 executionId 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    ResponseEntity<com.leets7th.job_is_be.global.response.ApiResponse<SyncJobExecution>> getSyncStatus(String executionId);

    @Operation(
            summary = "오늘의 추천 덱 생성",
            description = "로그인한 사용자를 대상으로 오늘의 추천 덱을 생성합니다. "
                    + "성향 퀴즈 결과를 페르소나로 삼아 pgvector 기반 추천 엔진을 실행하고, "
                    + "적합도·추천 이유·요약이 채워진 카드를 만듭니다. "
                    + "퀴즈 미완료면 onboarding_incomplete, 후보 0건이면 no_candidates 로 기록되고 카드는 생성되지 않습니다. "
                    + "06:00 자동 생성 배치가 붙기 전까지 수동 트리거용으로 사용합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<com.leets7th.job_is_be.global.response.ApiResponse<List<CardResponse>>> generateTodayDeck(
            @Parameter(hidden = true) Jwt jwt
    );
}
