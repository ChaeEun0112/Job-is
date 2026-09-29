package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.SyncJobExecution;
import com.leets7th.job_is_be.domain.job.dto.SyncJobStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SyncExecutionStore {

    private final ConcurrentHashMap<String, SyncJobExecution> store = new ConcurrentHashMap<>();

    public void register(String executionId) {
        store.put(executionId, SyncJobExecution.builder()
                .executionId(executionId)
                .status(SyncJobStatus.ACCEPTED)
                .acceptedAt(LocalDateTime.now())
                .pagesProcessed(0)
                .build());
    }

    public void markRunning(String executionId) {
        update(executionId, SyncJobStatus.RUNNING, 0, null, null);
    }

    public void markCompleted(String executionId, int pages) {
        update(executionId, SyncJobStatus.COMPLETED, pages, null, LocalDateTime.now());
    }

    public void markFailed(String executionId, int pages, String errorMessage) {
        update(executionId, SyncJobStatus.FAILED, pages, errorMessage, LocalDateTime.now());
    }

    public void markTimedOut(String executionId, int pages) {
        update(executionId, SyncJobStatus.TIMED_OUT, pages, null, LocalDateTime.now());
    }

    public Optional<SyncJobExecution> get(String executionId) {
        return Optional.ofNullable(store.get(executionId));
    }

    private void update(String executionId, SyncJobStatus status, int pages, String error, LocalDateTime finishedAt) {
        store.computeIfPresent(executionId, (id, existing) ->
                SyncJobExecution.builder()
                        .executionId(id)
                        .status(status)
                        .acceptedAt(existing.getAcceptedAt())
                        .finishedAt(finishedAt)
                        .pagesProcessed(pages)
                        .errorMessage(error)
                        .build()
        );
    }
}
