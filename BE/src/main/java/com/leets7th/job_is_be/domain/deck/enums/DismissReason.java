package com.leets7th.job_is_be.domain.deck.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum DismissReason {

    @Schema(description = "직무 불일치")
    JOB_MISMATCH,

    @Schema(description = "지역")
    LOCATION,

    @Schema(description = "경력 요건")
    EXPERIENCE,

    @Schema(description = "회사 규모")
    COMPANY_SIZE,

    @Schema(description = "이미 지원함")
    ALREADY_APPLIED,

    @Schema(description = "기타 (comment에 자유 입력)")
    OTHER
}
