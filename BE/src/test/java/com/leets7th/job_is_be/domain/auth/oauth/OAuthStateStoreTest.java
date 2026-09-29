package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.global.properties.OAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthStateStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private OAuthStateStore stateStore;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        stateStore = new OAuthStateStore(redisTemplate, properties());
    }

    @Test
    void createsUnpredictableStateWithFiveMinuteTtl() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        String first = stateStore.create(SocialType.KAKAO);
        String second = stateStore.create(SocialType.KAKAO);

        assertNotEquals(first, second);
        verify(valueOperations, org.mockito.Mockito.times(2)).set(
                keyCaptor.capture(),
                eq("KAKAO"),
                eq(Duration.ofMinutes(5))
        );
        assertTrue(keyCaptor.getAllValues().getFirst().endsWith(first));
    }

    @Test
    void consumesStateOnlyForMatchingProvider() {
        when(valueOperations.getAndDelete("auth:oauth:state:state"))
                .thenReturn("GOOGLE")
                .thenReturn(null);

        assertTrue(stateStore.consume("state", "state", SocialType.GOOGLE));
        assertFalse(stateStore.consume("state", "state", SocialType.GOOGLE));
        assertFalse(stateStore.consume("state", "different-state", SocialType.GOOGLE));
    }

    private OAuthProperties properties() {
        OAuthProperties.Provider provider = new OAuthProperties.Provider(
                "client", "secret", URI.create("http://localhost/callback"),
                URI.create("https://example.com/authorize"), URI.create("https://example.com/token"),
                URI.create("https://example.com/user"), List.of("email")
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
