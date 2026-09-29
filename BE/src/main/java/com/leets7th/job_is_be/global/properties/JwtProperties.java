package com.leets7th.job_is_be.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration,
        String cookieName,
        String cookiePath,
        boolean cookieSecure,
        String cookieSameSite
) {

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        if (accessTokenExpiration == null || accessTokenExpiration.isNegative() || accessTokenExpiration.isZero()) {
            throw new IllegalArgumentException("JWT access token expiration must be positive");
        }
        if (refreshTokenExpiration == null || refreshTokenExpiration.isNegative() || refreshTokenExpiration.isZero()) {
            throw new IllegalArgumentException("JWT refresh token expiration must be positive");
        }
    }
}
