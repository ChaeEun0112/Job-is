package com.leets7th.job_is_be.global.ai;

import com.leets7th.job_is_be.global.ai.dto.OpenAiRequest;
import com.leets7th.job_is_be.global.ai.dto.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiClientImpl implements OpenAiClient {

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;


    @Override
    public String chat(String systemPrompt, String userPrompt) {

        OpenAiRequest request = new OpenAiRequest(
                properties.model(),
                List.of(
                        new OpenAiRequest.Message(
                                "system",
                                systemPrompt
                        ),
                        new OpenAiRequest.Message(
                                "user",
                                userPrompt
                        )
                )
        );


        OpenAiResponse response = openAiRestClient.post()
                .uri("/chat/completions")
                .header(
                        "Authorization",
                        "Bearer " + properties.apiKey()
                )
                .header(
                        "Content-Type",
                        "application/json"
                )
                .body(request)
                .retrieve()
                .body(OpenAiResponse.class);


        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI 응답이 없습니다.");
        }

        return response.choices()
                .get(0)
                .message()
                .content();
    }
}