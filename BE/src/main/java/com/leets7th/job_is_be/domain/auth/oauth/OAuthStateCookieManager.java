package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.global.properties.JwtProperties;
import com.leets7th.job_is_be.global.properties.OAuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class OAuthStateCookieManager {

    private static final String COOKIE_NAME = "oauthState";
    private static final String COOKIE_PATH = "/api/auth/oauth";

    private final OAuthProperties oauthProperties;
    private final JwtProperties jwtProperties;

    public OAuthStateCookieManager(OAuthProperties oauthProperties, JwtProperties jwtProperties) {
        this.oauthProperties = oauthProperties;
        this.jwtProperties = jwtProperties;
    }

    public String extract(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public ResponseCookie create(String state) {
        return baseCookie(state)
                .maxAge(oauthProperties.stateTtl())
                .build();
    }

    public ResponseCookie clear() {
        return baseCookie("")
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite(jwtProperties.cookieSameSite())
                .path(COOKIE_PATH);
    }
}
