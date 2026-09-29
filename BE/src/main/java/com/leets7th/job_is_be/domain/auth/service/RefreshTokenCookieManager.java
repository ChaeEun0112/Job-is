package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.global.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieManager {

    private final JwtProperties properties;

    public RefreshTokenCookieManager(JwtProperties properties) {
        this.properties = properties;
    }

    public String extract(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (properties.cookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public ResponseCookie create(String refreshToken) {
        return baseCookie(refreshToken)
                .maxAge(properties.refreshTokenExpiration())
                .build();
    }

    public ResponseCookie clear() {
        return baseCookie("")
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(properties.cookieName(), value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite(properties.cookieSameSite())
                .path(properties.cookiePath());
    }
}
