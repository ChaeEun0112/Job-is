package com.leets7th.job_is_be.domain.mail.event;

import com.leets7th.job_is_be.domain.mail.enums.MailType;
import com.leets7th.job_is_be.domain.mail.service.MailDispatchService;
import com.leets7th.job_is_be.domain.mail.service.MailTemplateRenderer;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.service.NotificationSettingService;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class WelcomeMailEventListener {

    private final UserRepository userRepository;
    private final NotificationSettingService notificationSettingService;
    private final MailDispatchService dispatchService;
    private final MailTemplateRenderer templateRenderer;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OnboardingCompletedEvent event) {
        User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) {
            return;
        }
        NotificationSetting setting = notificationSettingService.getOrCreateForDelivery(user.getId());
        if (!setting.isEmailSubscribed()) {
            return;
        }
        dispatchService.send(
                user,
                MailType.WELCOME,
                "WELCOME:" + user.getId(),
                "Job.is에 오신 걸 환영해요",
                templateRenderer.welcome(setting.getUnsubscribeToken())
        );
    }
}
