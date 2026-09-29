package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeResponse;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthRestoreCodeStore;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserWithdrawal;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AccountRecoveryService {

    private final OAuthRestoreCodeStore restoreCodeStore;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenSessionStore refreshTokenSessionStore;

    public AccountRecoveryService(
            OAuthRestoreCodeStore restoreCodeStore,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserWithdrawalRepository userWithdrawalRepository,
            JwtTokenProvider tokenProvider,
            RefreshTokenSessionStore refreshTokenSessionStore
    ) {
        this.restoreCodeStore = restoreCodeStore;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userWithdrawalRepository = userWithdrawalRepository;
        this.tokenProvider = tokenProvider;
        this.refreshTokenSessionStore = refreshTokenSessionStore;
    }

    @Transactional
    public RestoreResult restore(String restoreCode) {
        if (restoreCode == null || restoreCode.isBlank()) {
            throw new GeneralException(ErrorStatus.OAUTH_RESTORE_CODE_MISSING);
        }
        OAuthRestoreCodeStore.RestorePayload payload = restoreCodeStore.consume(restoreCode)
                .orElseThrow(() -> new GeneralException(ErrorStatus.OAUTH_RESTORE_CODE_INVALID));
        User user = userRepository.findByIdForUpdate(payload.userId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        UserWithdrawal withdrawal = userWithdrawalRepository
                .findFirstByUserIdAndStatusOrderByRequestedAtDesc(
                        user.getId(),
                        WithdrawalStatus.PENDING
                )
                .orElseThrow(() -> new GeneralException(ErrorStatus.WITHDRAWAL_RESTORE_EXPIRED));

        OffsetDateTime now = OffsetDateTime.now();
        if (user.getStatus() != UserStatus.WITHDRAWN
                || payload.restorableUntil() == null
                || withdrawal.getScheduledDeletionAt() == null
                || !now.isBefore(payload.restorableUntil())
                || !now.isBefore(withdrawal.getScheduledDeletionAt())) {
            throw new GeneralException(ErrorStatus.WITHDRAWAL_RESTORE_EXPIRED);
        }

        boolean onboardingCompleted = userProfileRepository.findByUserId(user.getId())
                .map(profile -> profile.isOnboardingCompleted())
                .orElse(false);

        user.restore();
        withdrawal.restore(now);
        JwtTokenProvider.TokenPair tokenPair = tokenProvider.issueTokenPair(user.getId(), user.getRole().name());
        refreshTokenSessionStore.revokeAll(user.getId());
        refreshTokenSessionStore.save(
                tokenPair.refreshSessionId(),
                user.getId(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenTtl()
        );

        return new RestoreResult(
                new OAuthExchangeResponse(
                        tokenPair.accessToken(),
                        user.getId(),
                        false,
                        onboardingCompleted
                ),
                tokenPair.refreshToken()
        );
    }

    public record RestoreResult(
            OAuthExchangeResponse response,
            String refreshToken
    ) {
    }
}
