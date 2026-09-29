package com.leets7th.job_is_be.domain.personality.dto;

import java.util.List;

public record QuizApplyResponse(
        Long testId,
        String resultType,
        List<String> resultTags,
        boolean jobTestCompleted,
        boolean applied
) {
}
