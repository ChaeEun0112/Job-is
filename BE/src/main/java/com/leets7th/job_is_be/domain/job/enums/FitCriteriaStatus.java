package com.leets7th.job_is_be.domain.job.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 적합도 체크리스트 각 축의 판정값 (화면설계서 DET-01 ④ 당신의 기준 매트릭스).
 *
 * <p>기존에는 "✓" / "~" / "!" 문자열을 그대로 응답에 실었으나, 표시 기호가 응답 계약에 섞이면
 * 화면 표기를 바꿀 때 API 계약까지 흔들리므로 의미 단위 enum 으로 분리한다.
 */
public enum FitCriteriaStatus {

    @Schema(description = "충족 — 하드 근거로 확인됨")
    MATCH,

    @Schema(description = "추정 — 저신뢰 근거로만 추론됨. 화면에 '추정' 표기 필요")
    ESTIMATED,

    @Schema(description = "주의 — 상충하거나 판단 불가한 조건")
    CAUTION,

    @Schema(description = "정보 없음 — 판정 근거 자체가 없음. 해당 축 생략 가능")
    UNKNOWN
}
