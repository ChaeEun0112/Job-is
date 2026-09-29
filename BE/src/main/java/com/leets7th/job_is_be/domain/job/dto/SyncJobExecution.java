package com.leets7th.job_is_be.domain.job.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SyncJobExecution {
    private final String executionId;
    private final SyncJobStatus status;
    private final LocalDateTime acceptedAt;
    private final LocalDateTime finishedAt;
    private final int pagesProcessed;
    private final String errorMessage;
}
