package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.global.properties.OAuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

@Component
public class OAuthRestoreCodeStore {

    private static final String KEY_PREFIX = "auth:oauth:restore-code:";
    private static final int CODE_BYTES = 32;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OAuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthRestoreCodeStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OAuthProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public String create(Long userId, OffsetDateTime restorableUntil) {
        byte[] bytes = new byte[CODE_BYTES];
        secureRandom.nextBytes(bytes);
        String restoreCode = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redisTemplate.opsForValue().set(
                key(restoreCode),
                serialize(new RestorePayload(userId, restorableUntil.toString())),
                properties.loginCodeTtl()
        );
        return restoreCode;
    }

    public Optional<RestorePayload> consume(String restoreCode) {
        if (restoreCode == null || restoreCode.isBlank()) {
            return Optional.empty();
        }
        String payload = redisTemplate.opsForValue().getAndDelete(key(restoreCode));
        return payload == null ? Optional.empty() : Optional.of(deserialize(payload));
    }

    private String serialize(RestorePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize OAuth restore payload", e);
        }
    }

    private RestorePayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, RestorePayload.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize OAuth restore payload", e);
        }
    }

    private String key(String restoreCode) {
        return KEY_PREFIX + restoreCode;
    }

    public record RestorePayload(
            Long userId,
            String restorableUntilText
    ) {
        public OffsetDateTime restorableUntil() {
            return OffsetDateTime.parse(restorableUntilText);
        }
    }
}
