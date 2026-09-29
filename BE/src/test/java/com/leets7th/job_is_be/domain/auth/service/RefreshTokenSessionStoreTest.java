package com.leets7th.job_is_be.domain.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenSessionStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private RefreshTokenSessionStore sessionStore;

    @BeforeEach
    void setUp() {
        sessionStore = new RefreshTokenSessionStore(redisTemplate);
    }

    @Test
    void storesHashInsteadOfRawRefreshTokenAndConsumesOnlyOnce() {
        String rawToken = "raw-refresh-token";
        Duration ttl = Duration.ofDays(14);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        sessionStore.save("session-id", 1L, rawToken, ttl);
        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq("auth:refresh:session-id"),
                valueCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(ttl)
        );
        verify(setOperations).add("auth:refresh:user:1", "session-id");
        verify(redisTemplate).expire("auth:refresh:user:1", ttl);

        assertFalse(valueCaptor.getValue().contains(rawToken));

        when(valueOperations.getAndDelete("auth:refresh:session-id"))
                .thenReturn(valueCaptor.getValue())
                .thenReturn(null);

        assertTrue(sessionStore.consume("session-id", 1L, rawToken));
        assertFalse(sessionStore.consume("session-id", 1L, rawToken));
        verify(setOperations, org.mockito.Mockito.times(2))
                .remove("auth:refresh:user:1", "session-id");
    }

    @Test
    void rotatesRefreshSessionWithSingleRedisScript() {
        Duration ttl = Duration.ofDays(14);
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> oldValueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newValueCaptor = ArgumentCaptor.forClass(String.class);

        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.<String>any(),
                org.mockito.ArgumentMatchers.<String>any(),
                org.mockito.ArgumentMatchers.<String>any(),
                org.mockito.ArgumentMatchers.<String>any(),
                org.mockito.ArgumentMatchers.<String>any()
        )).thenReturn(1L);

        assertTrue(sessionStore.rotate(
                "old-session",
                1L,
                "old-refresh-token",
                "new-session",
                "new-refresh-token",
                ttl
        ));

        verify(redisTemplate).execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                keysCaptor.capture(),
                oldValueCaptor.capture(),
                newValueCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(String.valueOf(ttl.toMillis())),
                org.mockito.ArgumentMatchers.eq("old-session"),
                org.mockito.ArgumentMatchers.eq("new-session")
        );
        assertEquals(
                List.of(
                        "auth:refresh:old-session",
                        "auth:refresh:new-session",
                        "auth:refresh:user:1"
                ),
                keysCaptor.getValue()
        );
        assertFalse(oldValueCaptor.getValue().contains("old-refresh-token"));
        assertFalse(newValueCaptor.getValue().contains("new-refresh-token"));
    }

    @Test
    void revokesAllRefreshSessionsForUser() {
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.<String>any()
        )).thenReturn(2L);

        assertEquals(2L, sessionStore.revokeAll(1L));

        verify(redisTemplate).execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                org.mockito.ArgumentMatchers.eq(List.of("auth:refresh:user:1")),
                org.mockito.ArgumentMatchers.eq("auth:refresh:")
        );
    }
}
