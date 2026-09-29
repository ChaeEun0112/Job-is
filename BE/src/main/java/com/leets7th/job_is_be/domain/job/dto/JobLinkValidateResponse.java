package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "원문 링크 유효성 검증 응답 DTO")
public record JobLinkValidateResponse(
        @Schema(description = "링크 유효 여부 (정상 접속 가능 시 true)", example = "true")
        @JsonProperty("isValid")
        boolean isValid,

        @Schema(description = "HTTP 응답 상태 코드 (타임아웃/연결실패 시 null 가능)", example = "200")
        Integer statusCode,

        @Schema(description = "검증 결과 메시지", example = "정상 접근 가능한 링크입니다.")
        String message
) {
    public static JobLinkValidateResponse of(boolean isValid, Integer statusCode, String message) {
        return new JobLinkValidateResponse(isValid, statusCode, message);
    }
}
