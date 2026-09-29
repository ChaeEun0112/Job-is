package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.enums.FitCriteriaStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 적합도 체크리스트 6축 판정 (화면설계서 DET-01 ④).
 *
 * <p>설계서 불변 규칙 2가지를 지킨다.
 * <ul>
 *   <li>{@code skillsInferred=true} 근거는 무조건 {@link FitCriteriaStatus#ESTIMATED}</li>
 *   <li>급여는 DB 미보유이므로 항상 {@link FitCriteriaStatus#CAUTION}</li>
 * </ul>
 */
@Schema(description = "적합도 체크리스트 6축 판정")
public record CriteriaMatrixDto(
        @Schema(description = "직무 판정")
        FitCriteriaStatus jobType,

        @Schema(description = "경력 판정")
        FitCriteriaStatus career,

        @Schema(description = "지역 판정")
        FitCriteriaStatus location,

        @Schema(description = "스킬 판정")
        FitCriteriaStatus skills,

        @Schema(description = "선호 조건 판정")
        FitCriteriaStatus preference,

        @Schema(description = "급여 판정 — 급여 데이터 미보유로 항상 CAUTION")
        FitCriteriaStatus salary
) {
    public static CriteriaMatrixDto createDefault() {
        return new CriteriaMatrixDto(
                FitCriteriaStatus.UNKNOWN,
                FitCriteriaStatus.UNKNOWN,
                FitCriteriaStatus.UNKNOWN,
                FitCriteriaStatus.UNKNOWN,
                FitCriteriaStatus.UNKNOWN,
                FitCriteriaStatus.CAUTION
        );
    }
}
