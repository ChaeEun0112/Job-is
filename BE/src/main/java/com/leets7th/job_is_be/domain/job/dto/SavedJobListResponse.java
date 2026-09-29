package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.global.response.PageResponse;

public record SavedJobListResponse(
        long totalSaved,
        long totalApplyIntent,
        PageResponse<SavedJobResponse> saves
) {
}
