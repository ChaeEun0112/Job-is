package com.leets7th.job_is_be.global.jwt;

import com.leets7th.job_is_be.global.config.JwtConfig;
import com.leets7th.job_is_be.global.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private JwtDecoder accessTokenDecoder;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-secret-key-must-be-at-least-32-bytes-long",
                "job-is-be-test",
                Duration.ofMinutes(30),
                Duration.ofDays(14),
                "refreshToken",
                "/api/auth",
                false,
                "Lax"
        );
        JwtConfig config = new JwtConfig();
        SecretKey secretKey = config.jwtSecretKey(properties);
        JwtDecoder tokenDecoder = config.tokenDecoder(secretKey, properties);
        accessTokenDecoder = config.accessTokenDecoder(secretKey, properties);
        tokenProvider = new JwtTokenProvider(
                config.jwtEncoder(secretKey),
                tokenDecoder,
                properties,
                Clock.systemUTC()
        );
    }

    @Test
    void issuesAccessAndRefreshTokensWithDifferentTypes() {
        JwtTokenProvider.TokenPair pair = tokenProvider.issueTokenPair(1L, "ADMIN");

        Jwt accessToken = accessTokenDecoder.decode(pair.accessToken());
        JwtTokenProvider.RefreshTokenClaims refreshClaims =
                tokenProvider.decodeRefreshToken(pair.refreshToken());

        assertEquals("1", accessToken.getSubject());
        assertEquals("ACCESS", accessToken.getClaimAsString("tokenType"));
        assertEquals("ADMIN", accessToken.getClaimAsString("role"));
        assertEquals(1L, refreshClaims.userId());
        assertEquals(pair.refreshSessionId(), refreshClaims.sessionId());
        assertEquals(1800, pair.accessTokenExpiresIn());
        assertNotEquals(accessToken.getId(), pair.refreshSessionId());
    }

    @Test
    void rejectsRefreshTokenAsAccessToken() {
        JwtTokenProvider.TokenPair pair = tokenProvider.issueTokenPair(1L, "USER");

        assertThrows(JwtException.class, () -> accessTokenDecoder.decode(pair.refreshToken()));
    }
}
