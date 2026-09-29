package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.global.properties.OAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthLoginCodeStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private OAuthLoginCodeStore loginCodeStore;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        loginCodeStore = new OAuthLoginCodeStore(
                redisTemplate,
                new ObjectMapper(),
                properties()
        );
    }

    @Test
    void storesLoginCodeForOneMinute() {
        OAuthLoginCodeStore.LoginPayload payload = payload();

        String loginCode = loginCodeStore.create(payload);

        assertFalse(loginCode.isBlank());
        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq("auth:oauth:login-code:" + loginCode),
                anyString(),
                org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(1))
        );
    }

    @Test
    void consumesLoginCodeOnlyOnce() throws Exception {
        String storedPayload = new ObjectMapper().writeValueAsString(payload());
        when(valueOperations.getAndDelete("auth:oauth:login-code:code"))
                .thenReturn(storedPayload, (String) null);

        Optional<OAuthLoginCodeStore.LoginPayload> first = loginCodeStore.consume("code");
        Optional<OAuthLoginCodeStore.LoginPayload> second = loginCodeStore.consume("code");

        assertTrue(first.isPresent());
        assertEquals(1L, first.orElseThrow().userId());
        assertFalse(second.isPresent());
    }

    private OAuthLoginCodeStore.LoginPayload payload() {
        return new OAuthLoginCodeStore.LoginPayload(
                1L,
                true,
                false,
                "access",
                "refresh",
                "session",
                1800,
                Duration.ofDays(14).toSeconds()
        );
    }

    private OAuthProperties properties() {
        OAuthProperties.Provider provider = new OAuthProperties.Provider(
                "client-id",
                "client-secret",
                URI.create("http://localhost/callback"),
                URI.create("http://localhost/authorize"),
                URI.create("http://localhost/token"),
                URI.create("http://localhost/user"),
                List.of("email")
        );
        return new OAuthProperties(
                URI.create("http://localhost/success"),
                URI.create("http://localhost/failure"),
                Duration.ofMinutes(5),
                Duration.ofMinutes(1),
                Duration.ofSeconds(3),
                Duration.ofSeconds(5),
                provider,
                provider
        );
    }
}
