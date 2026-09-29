package com.leets7th.job_is_be.domain.auth.dto;

import com.leets7th.job_is_be.domain.user.enums.WithdrawalReasonCode;
import jakarta.validation.constraints.Size;

public record WithdrawalRequest(
        WithdrawalReasonCode reasonCode,
        @Size(max = 500) String reasonDetail
) {
}
