package com.leets7th.job_is_be.domain.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "원문 링크 유효성 검증 요청 DTO")
public record JobLinkValidateRequest(
        @Schema(description = "검증할 원문 공고 URL", example = "https://example.com/jobs/123")
        @NotBlank(message = "검증할 URL은 필수 입력값입니다.")
        @Pattern(
                regexp = "^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
                message = "올바른 URL 형식이 아닙니다."
        )
        String url
) {
}
