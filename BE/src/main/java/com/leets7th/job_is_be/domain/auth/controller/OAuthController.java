package com.leets7th.job_is_be.domain.auth.controller;

import com.leets7th.job_is_be.domain.auth.controller.docs.OAuthControllerDocs;
import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeRequest;
import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeResponse;
import com.leets7th.job_is_be.domain.auth.service.OAuthLoginService;
import com.leets7th.job_is_be.domain.auth.service.RefreshTokenCookieManager;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthStateCookieManager;
import com.leets7th.job_is_be.global.base.BaseStatus;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.properties.OAuthProperties;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth/oauth")
public class OAuthController implements OAuthControllerDocs {

    private final OAuthLoginService oauthLoginService;
    private final RefreshTokenCookieManager cookieManager;
    private final OAuthStateCookieManager stateCookieManager;
    private final OAuthProperties properties;

    public OAuthController(
            OAuthLoginService oauthLoginService,
            RefreshTokenCookieManager cookieManager,
            OAuthStateCookieManager stateCookieManager,
            OAuthProperties properties
    ) {
        this.oauthLoginService = oauthLoginService;
        this.cookieManager = cookieManager;
        this.stateCookieManager = stateCookieManager;
        this.properties = properties;
    }

    @GetMapping("/{provider}")
    public ResponseEntity<Void> authorize(@PathVariable String provider) {
        OAuthLoginService.AuthorizationRequest authorization =
                oauthLoginService.createAuthorizationRequest(provider);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authorization.uri().toString())
                .header(HttpHeaders.SET_COOKIE, stateCookieManager.create(authorization.state()).toString())
                .build();
    }

    @GetMapping("/{provider}/callback")
    public ResponseEntity<Void> callback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request
    ) {
        if (error != null && !error.isBlank()) {
            return redirectFailure(ErrorStatus.OAUTH_PROVIDER_ERROR);
        }

        try {
            OAuthLoginService.OAuthLoginResult result = oauthLoginService.login(
                    provider,
                    code,
                    state,
                    stateCookieManager.extract(request)
            );
            String fragment = result.restorationRequired()
                    ? "restoreCode=" + result.restoreCode()
                    + "&restorableUntil=" + result.restorableUntil()
                    : "code=" + result.loginCode();
            URI location = UriComponentsBuilder.fromUri(properties.frontendSuccessUri())
                    .fragment(fragment)
                    .build()
                    .encode()
                    .toUri();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, location.toString())
                    .header(HttpHeaders.SET_COOKIE, stateCookieManager.clear().toString())
                    .build();
        } catch (GeneralException e) {
            return redirectFailure(e.getErrorStatus());
        }
    }

    @PostMapping(value = "/exchange", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<OAuthExchangeResponse>> exchange(
            @RequestBody OAuthExchangeRequest request
    ) {
        OAuthLoginService.ExchangeResult result = oauthLoginService.exchange(request.loginCode());
        return withCookie(
                ApiResponse.success(SuccessStatus.OAUTH_EXCHANGE_SUCCESS, result.response()),
                cookieManager.create(result.refreshToken())
        );
    }

    private <T> ResponseEntity<ApiResponse<T>> withCookie(
            ResponseEntity<ApiResponse<T>> response,
            ResponseCookie cookie
    ) {
        return ResponseEntity
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response.getBody());
    }

    private ResponseEntity<Void> redirectFailure(BaseStatus errorStatus) {
        URI location = UriComponentsBuilder.fromUri(properties.frontendFailureUri())
                .queryParam("error", errorStatus.getCode())
                .build()
                .encode()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, location.toString())
                .header(HttpHeaders.SET_COOKIE, stateCookieManager.clear().toString())
                .build();
    }
}
