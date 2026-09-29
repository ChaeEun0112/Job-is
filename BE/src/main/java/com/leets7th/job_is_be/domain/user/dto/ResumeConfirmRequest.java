package com.leets7th.job_is_be.domain.user.dto;

import com.leets7th.job_is_be.domain.user.enums.ResumeCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResumeConfirmRequest(
        @NotBlank String objectKey,
        @NotBlank String fileName,
        @NotNull ResumeCategory category
) {
}
