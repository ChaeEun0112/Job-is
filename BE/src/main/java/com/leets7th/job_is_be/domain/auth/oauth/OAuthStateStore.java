package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.global.properties.OAuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class OAuthStateStore {

    private static final String KEY_PREFIX = "auth:oauth:state:";
    private static final int STATE_BYTES = 32;

    private final StringRedisTemplate redisTemplate;
    private final OAuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthStateStore(StringRedisTemplate redisTemplate, OAuthProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public String create(SocialType socialType) {
        byte[] bytes = new byte[STATE_BYTES];
        secureRandom.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        redisTemplate.opsForValue().set(
                key(state),
                socialType.name(),
                properties.stateTtl()
        );
        return state;
    }

    public boolean consume(String state, String cookieState, SocialType socialType) {
        if (state == null || state.isBlank() || cookieState == null || cookieState.isBlank()) {
            return false;
        }
        if (!MessageDigest.isEqual(
                state.getBytes(StandardCharsets.UTF_8),
                cookieState.getBytes(StandardCharsets.UTF_8)
        )) {
            return false;
        }
        String storedProvider = redisTemplate.opsForValue().getAndDelete(key(state));
        return socialType.name().equals(storedProvider);
    }

    private String key(String state) {
        return KEY_PREFIX + state;
    }
}
