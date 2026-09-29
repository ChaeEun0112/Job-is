package com.leets7th.job_is_be.domain.notification.service;

import com.leets7th.job_is_be.domain.notification.dto.UnsubscribeFeedbackRequest;
import com.leets7th.job_is_be.domain.notification.dto.UnsubscribeResponse;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.entity.UnsubscribeFeedback;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.notification.repository.UnsubscribeFeedbackRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 토큰 기반 수신거부(구독 해지) 처리.
 * 로그인 없이 이메일 링크에 담긴 토큰만으로 인증하며, 토큰은 회전하지 않으므로
 * 해지/재구독을 반복해도 같은 링크가 계속 유효하다 (멱등 처리).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnsubscribeService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UnsubscribeFeedbackRepository unsubscribeFeedbackRepository;

    @Transactional
    public UnsubscribeResponse unsubscribe(String token) {
        NotificationSetting setting = getByToken(token);
        if (setting.isEmailSubscribed()) {
            setting.updateEmailSubscribed(false);
        }
        return new UnsubscribeResponse(setting.isEmailSubscribed());
    }

    @Transactional
    public UnsubscribeResponse resubscribe(String token) {
        NotificationSetting setting = getByToken(token);
        if (!setting.isEmailSubscribed()) {
            setting.updateEmailSubscribed(true);
        }
        return new UnsubscribeResponse(setting.isEmailSubscribed());
    }

    @Transactional
    public void submitFeedback(String token, UnsubscribeFeedbackRequest request) {
        NotificationSetting setting = getByToken(token);
        unsubscribeFeedbackRepository.save(UnsubscribeFeedback.builder()
                .user(setting.getUser())
                .reason(request.reason())
                .comment(request.comment())
                .build());
    }

    private NotificationSetting getByToken(String token) {
        return notificationSettingRepository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNSUBSCRIBE_TOKEN_INVALID));
    }
}
