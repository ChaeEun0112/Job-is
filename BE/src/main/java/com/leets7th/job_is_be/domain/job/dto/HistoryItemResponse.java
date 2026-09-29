package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.enums.HistoryActionType;

import java.time.OffsetDateTime;

public record HistoryItemResponse(
        Long jobId,
        String companyName,
        String title,
        HistoryActionType actionType,
        String reasonCode,
        String comment,
        OffsetDateTime actionAt,
        boolean expired
) {
}
