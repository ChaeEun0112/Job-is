package com.leets7th.job_is_be.domain.job.dto;

import java.time.OffsetDateTime;

public record SavedJobResponse(
        Long jobId,
        String companyName,
        String title,
        String locationFull,
        String careerLevel,
        String employmentType,
        OffsetDateTime savedAt,
        OffsetDateTime deadlineAt,
        boolean expired,
        boolean applyIntent
) {
}
