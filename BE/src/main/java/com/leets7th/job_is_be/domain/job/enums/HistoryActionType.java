package com.leets7th.job_is_be.domain.job.enums;

import com.leets7th.job_is_be.domain.deck.enums.ActionType;

public enum HistoryActionType {
    VIEWED,
    SKIPPED,
    APPLY_INTENT,
    SAVED;

    public static HistoryActionType from(ActionType actionType) {
        return switch (actionType) {
            case VIEWED -> VIEWED;
            case DISMISSED -> SKIPPED;
            case APPLY_INTENT_CLICKED -> APPLY_INTENT;
            case SAVED -> SAVED;
            default -> throw new IllegalStateException("히스토리로 노출할 수 없는 액션 타입입니다: " + actionType);
        };
    }
}
