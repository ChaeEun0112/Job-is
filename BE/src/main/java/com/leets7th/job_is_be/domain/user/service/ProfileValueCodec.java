package com.leets7th.job_is_be.domain.user.service;

import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Component
public class ProfileValueCodec {

    private static final int COLUMN_LENGTH = 500;

    private final ObjectMapper objectMapper;

    public ProfileValueCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(List<String> values) {
        List<String> normalized = normalize(values);
        try {
            String encoded = objectMapper.writeValueAsString(normalized);
            if (encoded.length() > COLUMN_LENGTH) {
                throw new GeneralException(ErrorStatus.PROFILE_VALUE_TOO_LONG);
            }
            return encoded;
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize profile values", e);
        }
    }

    public List<String> decode(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return List.copyOf(Arrays.asList(objectMapper.readValue(value, String[].class)));
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize profile values", e);
        }
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
