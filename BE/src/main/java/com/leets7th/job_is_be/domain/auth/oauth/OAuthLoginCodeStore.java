package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.global.properties.OAuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Component
public class OAuthLoginCodeStore {

    private static final String KEY_PREFIX = "auth:oauth:login-code:";
    private static final int CODE_BYTES = 32;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OAuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthLoginCodeStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OAuthProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public String create(LoginPayload payload) {
        byte[] bytes = new byte[CODE_BYTES];
        secureRandom.nextBytes(bytes);
        String loginCode = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        redisTemplate.opsForValue().set(
                key(loginCode),
                serialize(payload),
                properties.loginCodeTtl()
        );
        return loginCode;
    }

    public Optional<LoginPayload> consume(String loginCode) {
        if (loginCode == null || loginCode.isBlank()) {
            return Optional.empty();
        }

        String storedPayload = redisTemplate.opsForValue().getAndDelete(key(loginCode));
        if (storedPayload == null) {
            return Optional.empty();
        }
        return Optional.of(deserialize(storedPayload));
    }

    private String serialize(LoginPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize OAuth login payload", e);
        }
    }

    private LoginPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, LoginPayload.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize OAuth login payload", e);
        }
    }

    private String key(String loginCode) {
        return KEY_PREFIX + loginCode;
    }

    public record LoginPayload(
            Long userId,
            boolean newUser,
            boolean onboardingCompleted,
            String accessToken,
            String refreshToken,
            String refreshSessionId,
            long accessTokenExpiresIn,
            long refreshTokenTtlSeconds
    ) {
        public Duration refreshTokenTtl() {
            return Duration.ofSeconds(refreshTokenTtlSeconds);
        }
    }
}
