package com.leets7th.job_is_be.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiConfig {

    @Bean
    public RestClient openAiRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

}
