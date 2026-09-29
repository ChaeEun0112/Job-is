package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.AccountResponse;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalRequest;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserWithdrawal;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalReasonCode;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.repository.UserConsentRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserConsentRepository userConsentRepository;
    @Mock
    private UserWithdrawalRepository userWithdrawalRepository;
    @Mock
    private RefreshTokenSessionStore refreshTokenSessionStore;
    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(
                userRepository,
                userConsentRepository,
                userWithdrawalRepository,
                refreshTokenSessionStore,
                notificationSettingRepository
        );
    }

    private User kakaoUser() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void 사용자가_없으면_예외를_던진다() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        GeneralException exception = catchThrowableOfType(
                () -> accountService.getAccount(1L), GeneralException.class);

        assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);
    }

    @Test
    void 계정_정보를_조회한다() {
        User user = kakaoUser();
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC);
        ReflectionTestUtils.setField(user, "createdAt", createdAt);
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        ReflectionTestUtils.setField(setting, "emailVerified", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        AccountResponse response = accountService.getAccount(1L);

        assertThat(response.socialType()).isEqualTo(SocialType.KAKAO);
        assertThat(response.joinedAt()).isEqualTo(createdAt);
        assertThat(response.receivingEmail()).isEqualTo("a@a.com");
        assertThat(response.emailVerified()).isTrue();
    }

    @Test
    void 알림_설정이_없으면_이메일_미확인_상태로_반환한다() {
        User user = kakaoUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.empty());

        AccountResponse response = accountService.getAccount(1L);

        assertThat(response.emailVerified()).isFalse();
    }

    @Test
    void withdrawsForThirtyDaysAndRevokesEveryRefreshSession() {
        User user = User.builder()
                .socialId("social-id")
                .socialType(SocialType.KAKAO)
                .email("user@example.com")
                .build();
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(userWithdrawalRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(
                1L,
                WithdrawalStatus.PENDING
        )).thenReturn(Optional.empty());
        OffsetDateTime before = OffsetDateTime.now().plusDays(30).minusSeconds(1);

        var response = accountService.withdraw(
                1L,
                new WithdrawalRequest(WithdrawalReasonCode.OTHER, "  직접 입력 사유  ")
        );

        OffsetDateTime after = OffsetDateTime.now().plusDays(30).plusSeconds(1);
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(response.restorableUntil()).isBetween(before, after);

        ArgumentCaptor<UserWithdrawal> withdrawalCaptor =
                ArgumentCaptor.forClass(UserWithdrawal.class);
        verify(userWithdrawalRepository).save(withdrawalCaptor.capture());
        UserWithdrawal withdrawal = withdrawalCaptor.getValue();
        assertThat(withdrawal.getReasonCode()).isEqualTo(WithdrawalReasonCode.OTHER);
        assertThat(withdrawal.getReasonDetail()).isEqualTo("직접 입력 사유");
        assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
        assertThat(withdrawal.getScheduledDeletionAt()).isEqualTo(response.restorableUntil());
        verify(refreshTokenSessionStore).revokeAll(1L);
    }
}
