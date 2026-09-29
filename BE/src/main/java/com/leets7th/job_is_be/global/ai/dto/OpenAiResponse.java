package com.leets7th.job_is_be.global.ai.dto;

import java.util.List;

public record OpenAiResponse(
        List<Choice> choices
) {

    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}