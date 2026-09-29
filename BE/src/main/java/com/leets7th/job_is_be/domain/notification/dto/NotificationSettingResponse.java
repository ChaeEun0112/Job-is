package com.leets7th.job_is_be.domain.notification.dto;

import java.time.LocalDate;

public record NotificationSettingResponse(
        boolean briefingEnabled,
        String sendSlot,
        boolean marketingSubscribed,
        SnoozeInfo snooze
) {
    public record SnoozeInfo(
            boolean snoozed,
            LocalDate until,
            boolean indefinite
    ) {
    }
}
