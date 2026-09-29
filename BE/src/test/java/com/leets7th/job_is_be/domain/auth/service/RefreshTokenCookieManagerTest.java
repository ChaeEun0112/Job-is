package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.global.properties.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenCookieManagerTest {

    @Test
    void createsHttpOnlyDevelopmentCookie() {
        RefreshTokenCookieManager manager = new RefreshTokenCookieManager(properties(false));

        ResponseCookie cookie = manager.create("refresh-token");

        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.isSecure());
        assertEquals("Lax", cookie.getSameSite());
        assertEquals("/api/auth", cookie.getPath());
        assertEquals(Duration.ofDays(14), cookie.getMaxAge());
    }

    @Test
    void clearsCookieWithZeroMaxAge() {
        RefreshTokenCookieManager manager = new RefreshTokenCookieManager(properties(true));

        ResponseCookie cookie = manager.clear();

        assertTrue(cookie.isSecure());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
    }

    private JwtProperties properties(boolean secure) {
        return new JwtProperties(
                "test-secret-key-must-be-at-least-32-bytes-long",
                "job-is-be-test",
                Duration.ofMinutes(30),
                Duration.ofDays(14),
                "refreshToken",
                "/api/auth",
                secure,
                "Lax"
        );
    }
}
