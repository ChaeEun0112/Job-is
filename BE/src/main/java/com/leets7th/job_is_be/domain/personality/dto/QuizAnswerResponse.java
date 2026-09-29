package com.leets7th.job_is_be.domain.personality.dto;

public record QuizAnswerResponse(
        Long testId,
        Integer questionNo,
        Integer choiceValue,
        int answeredCount,
        int totalCount,
        boolean completed
) {
}
