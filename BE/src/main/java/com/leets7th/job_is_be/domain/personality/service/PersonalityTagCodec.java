package com.leets7th.job_is_be.domain.personality.service;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Component
public class PersonalityTagCodec {

    private final ObjectMapper objectMapper;

    public PersonalityTagCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize personality tags", exception);
        }
    }

    public List<String> decode(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return List.copyOf(Arrays.asList(objectMapper.readValue(value, String[].class)));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to deserialize personality tags", exception);
        }
    }
}
