package com.leets7th.job_is_be.domain.notification.dto;

public record UnsubscribeResponse(
        boolean emailSubscribed // false = 수신거부 상태, true = 구독 중
) {
}
