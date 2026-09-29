package com.leets7th.job_is_be.domain.auth.controller;

import com.leets7th.job_is_be.domain.auth.controller.docs.AccountControllerDocs;
import com.leets7th.job_is_be.domain.auth.dto.AccountResponse;
import com.leets7th.job_is_be.domain.auth.dto.ConsentRequest;
import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeResponse;
import com.leets7th.job_is_be.domain.auth.dto.RestoreRequest;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalRequest;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalResponse;
import com.leets7th.job_is_be.domain.auth.service.AccountService;
import com.leets7th.job_is_be.domain.auth.service.AccountRecoveryService;
import com.leets7th.job_is_be.domain.auth.service.RefreshTokenCookieManager;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AccountController implements AccountControllerDocs {

    private final AccountService accountService;
    private final AccountRecoveryService accountRecoveryService;
    private final RefreshTokenCookieManager cookieManager;

    public AccountController(
            AccountService accountService,
            AccountRecoveryService accountRecoveryService,
            RefreshTokenCookieManager cookieManager
    ) {
        this.accountService = accountService;
        this.accountRecoveryService = accountRecoveryService;
        this.cookieManager = cookieManager;
    }

    @GetMapping("/account")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                SuccessStatus.ACCOUNT_GET_SUCCESS,
                accountService.getAccount(Long.valueOf(jwt.getSubject()))
        );
    }

    @PostMapping("/consent")
    public ResponseEntity<ApiResponse<Void>> consent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ConsentRequest request
    ) {
        accountService.saveConsent(Long.valueOf(jwt.getSubject()), request);
        return ApiResponse.success(SuccessStatus.CONSENT_SAVE_SUCCESS);
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> withdraw(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) WithdrawalRequest request
    ) {
        WithdrawalResponse response = accountService.withdraw(Long.valueOf(jwt.getSubject()), request);
        return withCookie(
                ApiResponse.success(SuccessStatus.WITHDRAWAL_SUCCESS, response),
                cookieManager.clear()
        );
    }

    @PostMapping("/restore")
    public ResponseEntity<ApiResponse<OAuthExchangeResponse>> restore(
            @Valid @RequestBody RestoreRequest request
    ) {
        AccountRecoveryService.RestoreResult result =
                accountRecoveryService.restore(request.restoreCode());
        return withCookie(
                ApiResponse.success(SuccessStatus.WITHDRAWAL_RESTORE_SUCCESS, result.response()),
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
}
