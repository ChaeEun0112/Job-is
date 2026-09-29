package com.leets7th.job_is_be.domain.deck.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 덱 빈 상태 원인 코드 (화면설계서 REC §0.3 · REC-07).
 * 프런트가 시각·후보 수를 조합해 자체 판정하지 않도록 서버가 이 코드 하나로만 내려준다.
 * 카드가 1건 이상이면 state 는 null 이며 정상 덱(REC-02)으로 렌더한다.
 */
@Getter
@RequiredArgsConstructor
public enum DeckState {

    /** 덱 생성 배치(06:00) 완료 전 접속 — "오늘의 레터가 곧 준비돼요" */
    PRE_SLOT("pre_slot"),

    /** 덱은 생성됐으나 조건에 맞는 후보가 0건 — "오늘은 딱 맞는 공고가 적어요" */
    NO_CANDIDATES("no_candidates"),

    /** 성향 퀴즈 미완료 등 온보딩 미완 — "첫 레터를 준비하고 있어요" */
    ONBOARDING_INCOMPLETE("onboarding_incomplete");

    private final String code;

    public static DeckState fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (DeckState state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        return null;
    }
}
