package com.leets7th.job_is_be.global.ai.dto;

import java.util.List;

public record OpenAiRequest(
        String model,
        List<Message> messages
) {

    public record Message(
            String role,
            String content
    ) {
    }
}