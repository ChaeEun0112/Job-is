package com.leets7th.job_is_be.domain.notification.dto;

import com.leets7th.job_is_be.domain.notification.enums.SnoozeDuration;

public record SnoozeRequest(
        SnoozeDuration duration
) {
}
