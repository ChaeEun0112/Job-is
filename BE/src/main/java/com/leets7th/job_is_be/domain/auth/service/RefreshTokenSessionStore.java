package com.leets7th.job_is_be.domain.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

@Component
public class RefreshTokenSessionStore {

    private static final String KEY_PREFIX = "auth:refresh:";
    private static final String USER_SESSION_KEY_PREFIX = "auth:refresh:user:";
    private static final String VALUE_SEPARATOR = ":";
    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if not current or current ~= ARGV[1] then
                return 0
            end
            redis.call('SET', KEYS[2], ARGV[2], 'PX', ARGV[3])
            redis.call('DEL', KEYS[1])
            redis.call('SREM', KEYS[3], ARGV[4])
            redis.call('SADD', KEYS[3], ARGV[5])
            redis.call('PEXPIRE', KEYS[3], ARGV[3])
            return 1
            """, Long.class);
    private static final DefaultRedisScript<Long> REVOKE_ALL_SCRIPT = new DefaultRedisScript<>("""
            local sessionIds = redis.call('SMEMBERS', KEYS[1])
            for _, sessionId in ipairs(sessionIds) do
                redis.call('DEL', ARGV[1] .. sessionId)
            end
            redis.call('DEL', KEYS[1])
            return #sessionIds
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenSessionStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String sessionId, Long userId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(
                key(sessionId),
                userId + VALUE_SEPARATOR + hash(refreshToken),
                ttl
        );
        SetOperations<String, String> sessions = redisTemplate.opsForSet();
        sessions.add(userSessionKey(userId), sessionId);
        redisTemplate.expire(userSessionKey(userId), ttl);
    }

    public boolean consume(String sessionId, Long userId, String refreshToken) {
        String storedValue = redisTemplate.opsForValue().getAndDelete(key(sessionId));
        redisTemplate.opsForSet().remove(userSessionKey(userId), sessionId);
        if (storedValue == null) {
            return false;
        }

        String expectedValue = userId + VALUE_SEPARATOR + hash(refreshToken);
        return MessageDigest.isEqual(
                storedValue.getBytes(StandardCharsets.UTF_8),
                expectedValue.getBytes(StandardCharsets.UTF_8)
        );
    }

    public boolean rotate(
            String oldSessionId,
            Long userId,
            String oldRefreshToken,
            String newSessionId,
            String newRefreshToken,
            Duration ttl
    ) {
        Long result = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(key(oldSessionId), key(newSessionId), userSessionKey(userId)),
                storedValue(userId, oldRefreshToken),
                storedValue(userId, newRefreshToken),
                String.valueOf(ttl.toMillis()),
                oldSessionId,
                newSessionId
        );
        return Long.valueOf(1L).equals(result);
    }

    public long revokeAll(Long userId) {
        Long revokedCount = redisTemplate.execute(
                REVOKE_ALL_SCRIPT,
                List.of(userSessionKey(userId)),
                KEY_PREFIX
        );
        return revokedCount == null ? 0 : revokedCount;
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private String userSessionKey(Long userId) {
        return USER_SESSION_KEY_PREFIX + userId;
    }

    private String storedValue(Long userId, String refreshToken) {
        return userId + VALUE_SEPARATOR + hash(refreshToken);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
