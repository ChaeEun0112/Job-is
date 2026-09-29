package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.oauth.OAuthRestoreCodeStore;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserWithdrawal;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountRecoveryServiceTest {

    @Mock
    private OAuthRestoreCodeStore restoreCodeStore;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserWithdrawalRepository userWithdrawalRepository;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RefreshTokenSessionStore refreshTokenSessionStore;

    private AccountRecoveryService recoveryService;

    @BeforeEach
    void setUp() {
        recoveryService = new AccountRecoveryService(
                restoreCodeStore,
                userRepository,
                userProfileRepository,
                userWithdrawalRepository,
                tokenProvider,
                refreshTokenSessionStore
        );
    }

    @Test
    void restoresAccountOnceAndIssuesNewTokenPair() {
        User user = User.builder()
                .socialId("social-id")
                .socialType(SocialType.KAKAO)
                .email("user@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.withdraw(OffsetDateTime.now().minusDays(1));
        UserWithdrawal withdrawal = UserWithdrawal.builder()
                .user(user)
                .requestedAt(OffsetDateTime.now().minusDays(1))
                .scheduledDeletionAt(OffsetDateTime.now().plusDays(29))
                .build();
        when(restoreCodeStore.consume("restore-code")).thenReturn(Optional.of(
                new OAuthRestoreCodeStore.RestorePayload(
                        1L,
                        withdrawal.getScheduledDeletionAt().toString()
                )
        ));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(userWithdrawalRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(
                1L,
                WithdrawalStatus.PENDING
        )).thenReturn(Optional.of(withdrawal));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(tokenProvider.issueTokenPair(1L, "USER")).thenReturn(new JwtTokenProvider.TokenPair(
                "access",
                "refresh",
                "session",
                1800,
                Duration.ofDays(14)
        ));

        AccountRecoveryService.RestoreResult result = recoveryService.restore("restore-code");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.RESTORED);
        assertThat(result.response().accessToken()).isEqualTo("access");
        assertThat(result.response().isNewUser()).isFalse();
        assertThat(result.refreshToken()).isEqualTo("refresh");
        verify(refreshTokenSessionStore).save(
                "session",
                1L,
                "refresh",
                Duration.ofDays(14)
        );
        verify(refreshTokenSessionStore).revokeAll(1L);
    }

    @Test
    void doesNotCreateRedisSessionWhenProfileLookupFails() {
        User user = User.builder()
                .socialId("social-id")
                .socialType(SocialType.KAKAO)
                .email("user@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.withdraw(OffsetDateTime.now().minusDays(1));
        UserWithdrawal withdrawal = UserWithdrawal.builder()
                .user(user)
                .requestedAt(OffsetDateTime.now().minusDays(1))
                .scheduledDeletionAt(OffsetDateTime.now().plusDays(29))
                .build();
        when(restoreCodeStore.consume("restore-code")).thenReturn(Optional.of(
                new OAuthRestoreCodeStore.RestorePayload(
                        1L,
                        withdrawal.getScheduledDeletionAt().toString()
                )
        ));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(userWithdrawalRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(
                1L,
                WithdrawalStatus.PENDING
        )).thenReturn(Optional.of(withdrawal));
        when(userProfileRepository.findByUserId(1L))
                .thenThrow(new IllegalStateException("database read failed"));

        assertThatThrownBy(() -> recoveryService.restore("restore-code"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
        verify(refreshTokenSessionStore, never()).revokeAll(1L);
        verify(refreshTokenSessionStore, never()).save(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
