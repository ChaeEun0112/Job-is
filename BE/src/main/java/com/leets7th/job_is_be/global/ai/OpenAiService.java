package com.leets7th.job_is_be.global.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final OpenAiClient openAiClient;


    public String chat(
            String systemPrompt,
            String userPrompt
    ) {
        return openAiClient.chat(
                systemPrompt,
                userPrompt
        );
    }
}