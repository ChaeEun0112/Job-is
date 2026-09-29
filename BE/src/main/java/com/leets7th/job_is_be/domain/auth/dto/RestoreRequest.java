package com.leets7th.job_is_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RestoreRequest(
        @NotBlank String restoreCode
) {
}
