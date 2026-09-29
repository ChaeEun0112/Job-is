package com.leets7th.job_is_be.domain.mail.service;

import com.leets7th.job_is_be.domain.mail.enums.MailType;
import com.leets7th.job_is_be.domain.mail.gateway.MailGateway;
import com.leets7th.job_is_be.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class MailDispatchService {

    private final MailDeliveryLogService deliveryLogService;
    private final MailGateway mailGateway;

    public boolean send(User user, MailType mailType, String deliveryKey, String subject, String html) {
        Long deliveryId = deliveryLogService.claim(user, mailType, deliveryKey);
        if (deliveryId == null) {
            return false;
        }
        try {
            mailGateway.send(user.getEmail(), subject, html);
            deliveryLogService.markSent(deliveryId);
            return true;
        } catch (RuntimeException exception) {
            deliveryLogService.markFailed(deliveryId, exception);
            log.error("메일 발송 실패: type={}, userId={}", mailType, user.getId(), exception);
            return false;
        }
    }
}
