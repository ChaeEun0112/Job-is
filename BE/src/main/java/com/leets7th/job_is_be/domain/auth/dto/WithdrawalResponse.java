package com.leets7th.job_is_be.domain.auth.dto;

import java.time.OffsetDateTime;

public record WithdrawalResponse(
        OffsetDateTime restorableUntil
) {
}
