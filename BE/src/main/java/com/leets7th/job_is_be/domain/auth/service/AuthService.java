package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.SessionResponse;
import com.leets7th.job_is_be.domain.auth.dto.TokenReissueResponse;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenSessionStore sessionStore;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;

    public AuthService(
            JwtTokenProvider tokenProvider,
            RefreshTokenSessionStore sessionStore,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserWithdrawalRepository userWithdrawalRepository
    ) {
        this.tokenProvider = tokenProvider;
        this.sessionStore = sessionStore;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userWithdrawalRepository = userWithdrawalRepository;
    }

    public ReissueResult reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_MISSING);
        }

        JwtTokenProvider.RefreshTokenClaims claims = decodeRefreshToken(refreshToken);
        User user = userRepository.findById(claims.userId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new GeneralException(ErrorStatus.WITHDRAWN_ACCOUNT_TOKEN_REISSUE);
        }

        JwtTokenProvider.TokenPair tokenPair = tokenProvider.issueTokenPair(claims.userId(), user.getRole().name());
        if (!sessionStore.rotate(
                claims.sessionId(),
                claims.userId(),
                refreshToken,
                tokenPair.refreshSessionId(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenTtl()
        )) {
            throw new GeneralException(ErrorStatus.REFRESH_SESSION_NOT_FOUND);
        }

        return new ReissueResult(
                new TokenReissueResponse(
                        tokenPair.accessToken(),
                        "Bearer",
                        tokenPair.accessTokenExpiresIn()
                ),
                tokenPair.refreshToken()
        );
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        try {
            JwtTokenProvider.RefreshTokenClaims claims = tokenProvider.decodeRefreshToken(refreshToken);
            sessionStore.consume(claims.sessionId(), claims.userId(), refreshToken);
        } catch (JwtException | IllegalArgumentException ignored) {
            // Logout remains idempotent even when the cookie has already expired.
        }
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        boolean onboardingCompleted = userProfileRepository.findByUserId(userId)
                .map(profile -> profile.isOnboardingCompleted())
                .orElse(false);

        var restorableUntil = userWithdrawalRepository
                .findFirstByUserIdAndStatusOrderByRequestedAtDesc(userId, WithdrawalStatus.PENDING)
                .map(withdrawal -> withdrawal.getScheduledDeletionAt())
                .orElse(null);

        return new SessionResponse(
                user.getId(),
                user.getEmail(),
                user.getSocialType(),
                user.getStatus(),
                onboardingCompleted,
                restorableUntil
        );
    }

    private JwtTokenProvider.RefreshTokenClaims decodeRefreshToken(String refreshToken) {
        try {
            return tokenProvider.decodeRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_INVALID);
        }
    }

    public record ReissueResult(TokenReissueResponse response, String refreshToken) {
    }
}
