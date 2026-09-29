package com.leets7th.job_is_be.domain.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingSyncService {

    private final JobSyncPageProcessor pageProcessor;
    private final SyncExecutionStore executionStore;

    @Value("${app.sync.timeout-minutes:30}")
    private int timeoutMinutes;

    public void syncOnStartup() {
        sync();
    }

    // 자동 트리거(@Scheduled, @EventListener) 제거함: JobPosting은 크롤러가 값을 채우지 않아
    // 항상 비어 있고, 이 상태로 동기화가 돌면 Job의 정상 데이터를 null로 덮어쓰거나
    // title(NOT NULL) 제약 위반으로 실패한다. JobParserService가 Job에 직접 저장하는
    // 현재 구조에서는 이 동기화가 필요 없다.
    // job_postings에는 동기화 여부를 나타내는 플래그가 없어, 매 실행마다 전체를 훑어
    // (source, externalId) 기준으로 upsert한다
    public void sync() {
        int page = 0;
        while (pageProcessor.processPage(page++)) {
        }
    }

    @Async("syncTaskExecutor")
    public void syncAsync(String executionId) {
        executionStore.markRunning(executionId);
        long deadline = System.currentTimeMillis() + (long) timeoutMinutes * 60 * 1_000;
        int page = 0;
        try {
            while (pageProcessor.processPage(page++)) {
                if (System.currentTimeMillis() > deadline) {
                    log.warn("동기화 타임아웃: executionId={}, 처리 페이지 수={}", executionId, page);
                    executionStore.markTimedOut(executionId, page);
                    return;
                }
            }
            executionStore.markCompleted(executionId, page);
            log.info("동기화 완료: executionId={}, 처리 페이지 수={}", executionId, page);
        } catch (Exception e) {
            log.error("동기화 실패: executionId={}, 처리 페이지 수={}, 원인={}", executionId, page, e.getMessage(), e);
            executionStore.markFailed(executionId, page, e.getMessage());
        }
    }
}
