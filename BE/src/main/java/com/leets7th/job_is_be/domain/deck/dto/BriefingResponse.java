package com.leets7th.job_is_be.domain.deck.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * REC-01 인트로 / REC-07 빈 상태 응답 — 화면설계서 REC §0.3 데이터 계약.
 * NULL 필드 = 해당 카피 줄/요소 생략이 원칙이다.
 */
public record BriefingResponse(
        @Schema(description = "덱 식별자. 덱 미생성 시 null")
        Long deckId,

        @Schema(description = "덱 기준 일자")
        LocalDate deckDate,

        @Schema(description = "레터 회차 No.{n}. 산출 불가 시 null → 편지 번호 생략, 일자만 표기")
        Long deckSeq,

        @Schema(description = "빈 상태 원인 코드. null 이면 정상 덱(REC-02). "
                + "pre_slot / no_candidates / onboarding_incomplete 중 하나",
                example = "no_candidates")
        String state,

        @Schema(description = "최초 열람 시각. null 이면 당일 첫 방문(REC-01 인트로 노출), "
                + "값이 있으면 재방문(REC-06 인트로 생략)")
        OffsetDateTime firstOpenedAt,

        @Schema(description = "시간대별 인사말")
        String greeting,

        @Schema(description = "검토 건수 K — 지원 가능한 전체 공고 수. 산출 불가 시 해당 줄 생략")
        long applicableCount,

        @Schema(description = "추린 건수 N — 오늘 덱에 담긴 카드 수")
        int curatedCount,

        @Schema(description = "큐레이션 테마 한 줄. null 이면 테마 문장 생략")
        String theme
) {
}
