package com.leets7th.job_is_be.domain.personality.dto;

public record QuizAnswerRequest(
        Long testId,
        Integer questionNo,
        Integer choiceValue
) {
}
