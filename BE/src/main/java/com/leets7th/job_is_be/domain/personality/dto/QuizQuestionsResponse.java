package com.leets7th.job_is_be.domain.personality.dto;

import com.leets7th.job_is_be.domain.personality.enums.PersonalityTestSource;

import java.util.List;

public record QuizQuestionsResponse(
        Long testId,
        PersonalityTestSource source,
        boolean completed,
        int answeredCount,
        int totalCount,
        List<Question> questions
) {
    public record Question(
            Integer questionNo,
            String question,
            List<Choice> choices,
            Integer selectedChoiceValue
    ) {
    }

    public record Choice(
            Integer choiceValue,
            String content
    ) {
    }
}
