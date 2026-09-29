package com.leets7th.job_is_be.domain.notification.dto;

public record NotificationSettingUpdateRequest(
        Boolean briefingEnabled,
        String sendSlot,
        Boolean marketingSubscribed
) {
}
