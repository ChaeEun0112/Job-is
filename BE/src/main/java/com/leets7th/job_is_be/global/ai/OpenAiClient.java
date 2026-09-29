package com.leets7th.job_is_be.global.ai;

public interface OpenAiClient {

    String chat(String systemPrompt, String userPrompt);

}