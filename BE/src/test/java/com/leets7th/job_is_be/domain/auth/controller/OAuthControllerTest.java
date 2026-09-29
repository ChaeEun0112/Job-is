package com.leets7th.job_is_be.domain.auth.controller;

import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeRequest;
import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeResponse;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthStateCookieManager;
import com.leets7th.job_is_be.domain.auth.service.OAuthLoginService;
import com.leets7th.job_is_be.domain.auth.service.RefreshTokenCookieManager;
import com.leets7th.job_is_be.global.properties.OAuthProperties;
import com.leets7th.job_is_be.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthControllerTest {

    @Mock
    private OAuthLoginService oauthLoginService;
    @Mock
    private RefreshTokenCookieManager cookieManager;
    @Mock
    private OAuthStateCookieManager stateCookieManager;
    @Mock
    private HttpServletRequest request;

    private OAuthController controller;

    @BeforeEach
    void setUp() {
        controller = new OAuthController(
                oauthLoginService,
                cookieManager,
                stateCookieManager,
                properties()
        );
    }

    @Test
    void redirectsSuccessfulCallbackWithOneTimeLoginCode() {
        prepareStateCookieClear();
        when(stateCookieManager.extract(request)).thenReturn("state-cookie");
        when(oauthLoginService.login("kakao", "provider-code", "state", "state-cookie"))
                .thenReturn(OAuthLoginService.OAuthLoginResult.login("login-code"));

        ResponseEntity<Void> response = controller.callback(
                "kakao",
                "provider-code",
                "state",
                null,
                request
        );

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(
                "http://localhost:5173/oauth/callback#code=login-code",
                response.getHeaders().getLocation().toString()
        );
        verify(cookieManager, never()).create(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void redirectsWithdrawnAccountWithOneTimeRestoreCode() {
        prepareStateCookieClear();
        when(stateCookieManager.extract(request)).thenReturn("state-cookie");
        when(oauthLoginService.login("kakao", "provider-code", "state", "state-cookie"))
                .thenReturn(OAuthLoginService.OAuthLoginResult.restoration(
                        "restore-code",
                        OffsetDateTime.of(2026, 8, 25, 12, 0, 0, 0, ZoneOffset.ofHours(9))
                ));

        ResponseEntity<Void> response = controller.callback(
                "kakao",
                "provider-code",
                "state",
                null,
                request
        );

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(
                "http://localhost:5173/oauth/callback"
                        + "#restoreCode=restore-code&restorableUntil=2026-08-25T12:00+09:00",
                response.getHeaders().getLocation().toString()
        );
    }

    @Test
    void redirectsFailedCallbackWithErrorCode() {
        prepareStateCookieClear();
        ResponseEntity<Void> response = controller.callback(
                "kakao",
                null,
                null,
                "access_denied",
                request
        );

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(
                "http://localhost:5173/oauth/callback?error=AUTH_502_1",
                response.getHeaders().getLocation().toString()
        );
    }

    @Test
    void exchangesLoginCodeAndSetsRefreshTokenCookie() {
        OAuthExchangeResponse exchangeResponse = new OAuthExchangeResponse(
                "access-token",
                1L,
                true,
                false
        );
        when(oauthLoginService.exchange("login-code"))
                .thenReturn(new OAuthLoginService.ExchangeResult(exchangeResponse, "refresh-token"));
        when(cookieManager.create("refresh-token")).thenReturn(
                ResponseCookie.from("refreshToken", "refresh-token")
                        .httpOnly(true)
                        .path("/api/auth")
                        .build()
        );

        ResponseEntity<ApiResponse<OAuthExchangeResponse>> response = controller.exchange(
                new OAuthExchangeRequest("login-code")
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTH_200_6", response.getBody().getCode());
        assertEquals("access-token", response.getBody().getData().accessToken());
        assertEquals(1L, response.getBody().getData().userId());
        assertTrue(response.getBody().getData().isNewUser());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("HttpOnly"));
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
                URI.create("http://localhost:5173/oauth/callback"),
                URI.create("http://localhost:5173/oauth/callback"),
                Duration.ofMinutes(5),
                Duration.ofMinutes(1),
                Duration.ofSeconds(3),
                Duration.ofSeconds(5),
                provider,
                provider
        );
    }

    private void prepareStateCookieClear() {
        when(stateCookieManager.clear()).thenReturn(
                ResponseCookie.from("oauthState", "").maxAge(0).build()
        );
    }
}
