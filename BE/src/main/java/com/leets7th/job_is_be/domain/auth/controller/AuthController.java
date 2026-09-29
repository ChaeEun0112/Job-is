package com.leets7th.job_is_be.domain.auth.controller;

import com.leets7th.job_is_be.domain.auth.controller.docs.AuthControllerDocs;
import com.leets7th.job_is_be.domain.auth.dto.CsrfTokenResponse;
import com.leets7th.job_is_be.domain.auth.dto.SessionResponse;
import com.leets7th.job_is_be.domain.auth.dto.TokenReissueResponse;
import com.leets7th.job_is_be.domain.auth.service.AuthService;
import com.leets7th.job_is_be.domain.auth.service.RefreshTokenCookieManager;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;
    private final RefreshTokenCookieManager cookieManager;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(
            AuthService authService,
            RefreshTokenCookieManager cookieManager,
            CsrfTokenRepository csrfTokenRepository
    ) {
        this.authService = authService;
        this.cookieManager = cookieManager;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<CsrfTokenResponse>> csrf(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        CsrfToken csrfToken = csrfTokenRepository.loadDeferredToken(request, response).get();
        return ApiResponse.success(
                SuccessStatus.CSRF_TOKEN_GET_SUCCESS,
                new CsrfTokenResponse(csrfToken.getToken(), csrfToken.getHeaderName())
        );
    }

    @PostMapping("/token/reissue")
    public ResponseEntity<ApiResponse<TokenReissueResponse>> reissue(HttpServletRequest request) {
        AuthService.ReissueResult result = authService.reissue(cookieManager.extract(request));
        return withCookie(
                ApiResponse.success(SuccessStatus.TOKEN_REISSUE_SUCCESS, result.response()),
                cookieManager.create(result.refreshToken())
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SessionResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(
                SuccessStatus.SESSION_GET_SUCCESS,
                authService.getSession(Long.valueOf(jwt.getSubject()))
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(cookieManager.extract(request));
        return withCookie(
                ApiResponse.success(SuccessStatus.LOGOUT_SUCCESS),
                cookieManager.clear()
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
}
