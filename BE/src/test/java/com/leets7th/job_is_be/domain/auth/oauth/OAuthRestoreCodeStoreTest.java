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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthRestoreCodeStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private OAuthRestoreCodeStore restoreCodeStore;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        restoreCodeStore = new OAuthRestoreCodeStore(
                redisTemplate,
                new ObjectMapper(),
                properties()
        );
    }

    @Test
    void storesAndConsumesRestoreCodeOnce() throws Exception {
        OffsetDateTime deadline = OffsetDateTime.of(2026, 8, 25, 12, 0, 0, 0, ZoneOffset.ofHours(9));
        String restoreCode = restoreCodeStore.create(1L, deadline);
        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq("auth:oauth:restore-code:" + restoreCode),
                anyString(),
                org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(1))
        );

        String payload = new ObjectMapper().writeValueAsString(
                new OAuthRestoreCodeStore.RestorePayload(1L, deadline.toString())
        );
        when(valueOperations.getAndDelete("auth:oauth:restore-code:code"))
                .thenReturn(payload, (String) null);

        var first = restoreCodeStore.consume("code");
        var second = restoreCodeStore.consume("code");

        assertThat(first).isPresent();
        assertThat(first.orElseThrow().userId()).isEqualTo(1L);
        assertThat(first.orElseThrow().restorableUntil()).isEqualTo(deadline);
        assertThat(second).isEmpty();
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
