package com.leets7th.job_is_be.domain.notification.service;

import com.leets7th.job_is_be.domain.notification.dto.UnsubscribeFeedbackRequest;
import com.leets7th.job_is_be.domain.notification.dto.UnsubscribeResponse;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.enums.UnsubscribeReason;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.notification.repository.UnsubscribeFeedbackRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnsubscribeServiceTest {

    @Mock
    private NotificationSettingRepository notificationSettingRepository;
    @Mock
    private UnsubscribeFeedbackRepository unsubscribeFeedbackRepository;

    private UnsubscribeService unsubscribeService;

    private User user() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private NotificationSetting setting() {
        NotificationSetting setting = NotificationSetting.builder().user(user()).sendSlot("07:30").build();
        return setting;
    }

    private UnsubscribeService service() {
        return new UnsubscribeService(notificationSettingRepository, unsubscribeFeedbackRepository);
    }

    @Test
    void 유효하지_않은_토큰이면_예외를_던진다() {
        when(notificationSettingRepository.findByUnsubscribeToken("bad-token")).thenReturn(Optional.empty());

        GeneralException exception = catchThrowableOfType(
                () -> service().unsubscribe("bad-token"), GeneralException.class);

        assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.UNSUBSCRIBE_TOKEN_INVALID);
    }

    @Test
    void 수신거부하면_emailSubscribed가_false가_된다() {
        NotificationSetting setting = setting();
        when(notificationSettingRepository.findByUnsubscribeToken(setting.getUnsubscribeToken()))
                .thenReturn(Optional.of(setting));

        UnsubscribeResponse response = service().unsubscribe(setting.getUnsubscribeToken());

        assertThat(response.emailSubscribed()).isFalse();
        assertThat(setting.isEmailSubscribed()).isFalse();
    }

    @Test
    void 이미_수신거부된_토큰으로_다시_요청해도_에러없이_같은_상태를_반환한다() {
        NotificationSetting setting = setting();
        setting.updateEmailSubscribed(false);
        when(notificationSettingRepository.findByUnsubscribeToken(setting.getUnsubscribeToken()))
                .thenReturn(Optional.of(setting));

        UnsubscribeResponse response = service().unsubscribe(setting.getUnsubscribeToken());

        assertThat(response.emailSubscribed()).isFalse();
    }

    @Test
    void 재구독하면_emailSubscribed가_true가_된다() {
        NotificationSetting setting = setting();
        setting.updateEmailSubscribed(false);
        when(notificationSettingRepository.findByUnsubscribeToken(setting.getUnsubscribeToken()))
                .thenReturn(Optional.of(setting));

        UnsubscribeResponse response = service().resubscribe(setting.getUnsubscribeToken());

        assertThat(response.emailSubscribed()).isTrue();
        assertThat(setting.isEmailSubscribed()).isTrue();
    }

    @Test
    void 해지_사유를_제출하면_사용자와_사유가_저장된다() {
        NotificationSetting setting = setting();
        when(notificationSettingRepository.findByUnsubscribeToken(setting.getUnsubscribeToken()))
                .thenReturn(Optional.of(setting));

        service().submitFeedback(
                setting.getUnsubscribeToken(),
                new UnsubscribeFeedbackRequest(UnsubscribeReason.TOO_FREQUENT, null)
        );

        ArgumentCaptor<com.leets7th.job_is_be.domain.notification.entity.UnsubscribeFeedback> captor =
                ArgumentCaptor.forClass(com.leets7th.job_is_be.domain.notification.entity.UnsubscribeFeedback.class);
        verify(unsubscribeFeedbackRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(setting.getUser());
        assertThat(captor.getValue().getReason()).isEqualTo(UnsubscribeReason.TOO_FREQUENT);
    }
}
