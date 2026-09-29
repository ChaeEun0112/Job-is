package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.service.RecommendationService;
import com.leets7th.job_is_be.domain.job.controller.docs.JobCrawlerControllerDocs;
import com.leets7th.job_is_be.domain.job.dto.SyncJobExecution;
import com.leets7th.job_is_be.domain.job.service.JobCrawlerManager;
import com.leets7th.job_is_be.domain.job.service.JobPostingSyncService;
import com.leets7th.job_is_be.domain.job.service.SyncExecutionStore;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin/crawler")
@RequiredArgsConstructor
public class JobCrawlerController implements JobCrawlerControllerDocs {

    private final JobCrawlerManager jobCrawlerManager;
    private final JobPostingSyncService jobPostingSyncService;
    private final SyncExecutionStore syncExecutionStore;
    private final RecommendationService recommendationService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<String>> triggerCrawlerPipeline() {
        log.info("관리자 요청: 전체 수집 파이프라인 수동 실행 API 호출됨");
        jobCrawlerManager.runPipelineAndSave();
        return ApiResponse.success(
                SuccessStatus.CRAWLER_PIPELINE_RUN_SUCCESS,
                "크롤링 및 DB 적재 프로세스가 완료되었습니다. 서버 로그를 확인하세요."
        );
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<SyncJobExecution>> syncJobPostings() {
        String executionId = UUID.randomUUID().toString();
        syncExecutionStore.register(executionId);
        try {
            jobPostingSyncService.syncAsync(executionId);
        } catch (TaskRejectedException e) {
            throw new GeneralException(ErrorStatus.SYNC_ALREADY_RUNNING);
        }
        log.info("관리자 요청: job_postings → jobs 비동기 동기화 시작, executionId={}", executionId);
        return ApiResponse.success(
                SuccessStatus.JOB_POSTING_SYNC_ACCEPTED,
                syncExecutionStore.get(executionId).orElseThrow()
        );
    }

    @GetMapping("/sync/{executionId}")
    public ResponseEntity<ApiResponse<SyncJobExecution>> getSyncStatus(@PathVariable String executionId) {
        SyncJobExecution execution = syncExecutionStore.get(executionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.SYNC_EXECUTION_NOT_FOUND));
        return ApiResponse.success(SuccessStatus.JOB_POSTING_SYNC_STATUS_SUCCESS, execution);
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<CardResponse>>> generateTodayDeck(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<CardResponse> response = recommendationService.generateTodayDeck(Long.valueOf(jwt.getSubject()));
        return ApiResponse.success(SuccessStatus.DECK_GENERATE_SUCCESS, response);
    }
}
