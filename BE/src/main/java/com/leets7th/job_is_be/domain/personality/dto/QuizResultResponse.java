package com.leets7th.job_is_be.domain.personality.dto;

import java.util.List;

public record QuizResultResponse(
        Long testId,
        boolean completed,
        int answeredCount,
        int totalCount,
        Scores scores,
        ResultType resultType,
        List<String> resultTags
) {
    public record Scores(
            int stabilityChallenge,
            int balanceImmersion,
            int expertAllRounder
    ) {
    }

    public record ResultType(
            String code,
            String name,
            String summary
    ) {
    }
}
