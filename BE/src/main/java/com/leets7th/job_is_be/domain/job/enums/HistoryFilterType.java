package com.leets7th.job_is_be.domain.job.enums;

import com.leets7th.job_is_be.domain.deck.enums.ActionType;

import java.util.List;

public enum HistoryFilterType {
    ALL,
    VIEWED,
    SKIPPED,
    APPLY_INTENT,
    SAVED;

    public List<ActionType> toActionTypes() {
        return switch (this) {
            case ALL -> List.of(ActionType.VIEWED, ActionType.DISMISSED, ActionType.APPLY_INTENT_CLICKED, ActionType.SAVED);
            case VIEWED -> List.of(ActionType.VIEWED);
            case SKIPPED -> List.of(ActionType.DISMISSED);
            case APPLY_INTENT -> List.of(ActionType.APPLY_INTENT_CLICKED);
            case SAVED -> List.of(ActionType.SAVED);
        };
    }
}
