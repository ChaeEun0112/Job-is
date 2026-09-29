package com.leets7th.job_is_be.domain.deck.dto;

import com.leets7th.job_is_be.domain.deck.enums.DismissReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DismissReasonRequest(
        @Schema(description = "관심없음 사유")
        @NotNull
        DismissReason reason,
        @Schema(description = "자유 코멘트, 최대 200자 (선택)")
        @Size(max = 200)
        String comment
) {
}
