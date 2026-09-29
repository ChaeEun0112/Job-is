package com.leets7th.job_is_be.domain.notification.dto;

import com.leets7th.job_is_be.domain.notification.enums.UnsubscribeReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnsubscribeFeedbackRequest(
        @NotNull
        UnsubscribeReason reason,
        @Size(max = 200)
        String comment // reason=OTHER일 때 자유 입력 (선택)
) {
}
