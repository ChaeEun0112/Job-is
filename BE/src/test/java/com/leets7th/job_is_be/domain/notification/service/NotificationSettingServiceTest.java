package com.leets7th.job_is_be.domain.notification.service;

import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingResponse;
import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingUpdateRequest;
import com.leets7th.job_is_be.domain.notification.dto.SnoozeRequest;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.enums.SnoozeDuration;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @Mock
    private NotificationSettingRepository notificationSettingRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    private User user() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void 사용자가_없으면_예외를_던진다() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        GeneralException exception = catchThrowableOfType(
                () -> notificationSettingService.getSetting(1L), GeneralException.class);

        assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);
    }

    @Test
    void 설정이_없으면_기본값으로_생성해서_조회한다() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.empty());
        when(notificationSettingRepository.saveAndFlush(any(NotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationSettingResponse response = notificationSettingService.getSetting(1L);

        assertThat(response.sendSlot()).isEqualTo("18:30");
        assertThat(response.briefingEnabled()).isTrue();
        assertThat(response.marketingSubscribed()).isFalse();
        assertThat(response.snooze().snoozed()).isFalse();
        verify(notificationSettingRepository).saveAndFlush(any(NotificationSetting.class));
    }

    @Test
    void 스누즈_종료일이_지나면_스누즈_상태가_아니다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        setting.snooze(LocalDate.now().minusDays(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        NotificationSettingResponse response = notificationSettingService.getSetting(1L);

        assertThat(response.snooze().snoozed()).isFalse();
        assertThat(response.snooze().until()).isNull();
    }

    @Test
    void 재개_예정일_당일에는_이미_재개된_것으로_본다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        setting.snooze(LocalDate.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        NotificationSettingResponse response = notificationSettingService.getSetting(1L);

        assertThat(response.snooze().snoozed()).isFalse();
        assertThat(response.snooze().until()).isNull();
    }

    @Test
    void 동시_생성_시_유니크_제약_위반이_발생하면_기존_설정을_재조회한다() {
        User user = user();
        NotificationSetting existing = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(notificationSettingRepository.saveAndFlush(any(NotificationSetting.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        NotificationSettingResponse response = notificationSettingService.getSetting(1L);

        assertThat(response.sendSlot()).isEqualTo("18:30");
        verify(notificationSettingRepository).saveAndFlush(any(NotificationSetting.class));
        verify(notificationSettingRepository, times(2)).findByUser(user);
    }

    @Test
    void 허용되지_않은_슬롯으로_변경하면_예외를_던진다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        NotificationSettingUpdateRequest request = new NotificationSettingUpdateRequest(null, "09:00", null);

        GeneralException exception = catchThrowableOfType(
                () -> notificationSettingService.updateSetting(1L, request), GeneralException.class);

        assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.NOTIFICATION_INVALID_SEND_SLOT);
    }

    @Test
    void 수신_설정을_변경한다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        NotificationSettingUpdateRequest request = new NotificationSettingUpdateRequest(false, "18:30", true);
        NotificationSettingResponse response = notificationSettingService.updateSetting(1L, request);

        assertThat(response.briefingEnabled()).isFalse();
        assertThat(response.sendSlot()).isEqualTo("18:30");
        assertThat(response.marketingSubscribed()).isTrue();
    }

    @Test
    void 스누즈_기간이_없으면_예외를_던진다() {
        GeneralException exception = catchThrowableOfType(
                () -> notificationSettingService.snooze(1L, new SnoozeRequest(null)), GeneralException.class);

        assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.NOTIFICATION_SNOOZE_DURATION_REQUIRED);
    }

    @Test
    void 스누즈를_7일_설정한다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        notificationSettingService.snooze(1L, new SnoozeRequest(SnoozeDuration.SEVEN_DAYS));

        assertThat(setting.getSnoozeUntil()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(setting.isSnoozeIndefinite()).isFalse();
    }

    @Test
    void 스누즈를_무기한_설정한다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        notificationSettingService.snooze(1L, new SnoozeRequest(SnoozeDuration.INDEFINITE));

        assertThat(setting.isSnoozeIndefinite()).isTrue();
        assertThat(setting.getSnoozeUntil()).isNull();
    }

    @Test
    void 재요청시_기존_스누즈_값을_덮어쓴다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        setting.snoozeIndefinitely();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        notificationSettingService.snooze(1L, new SnoozeRequest(SnoozeDuration.THIRTY_DAYS));

        assertThat(setting.isSnoozeIndefinite()).isFalse();
        assertThat(setting.getSnoozeUntil()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    void 스누즈를_해제하면_즉시_재개된다() {
        User user = user();
        NotificationSetting setting = NotificationSetting.builder().user(user).sendSlot("07:30").build();
        setting.snooze(LocalDate.now().plusDays(7));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.of(setting));

        notificationSettingService.cancelSnooze(1L);

        assertThat(setting.getSnoozeUntil()).isNull();
        assertThat(setting.isSnoozeIndefinite()).isFalse();
    }
}
