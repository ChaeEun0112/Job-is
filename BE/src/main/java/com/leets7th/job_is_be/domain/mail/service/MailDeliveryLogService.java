package com.leets7th.job_is_be.domain.mail.service;

import com.leets7th.job_is_be.domain.mail.entity.MailDelivery;
import com.leets7th.job_is_be.domain.mail.enums.MailType;
import com.leets7th.job_is_be.domain.mail.repository.MailDeliveryRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MailDeliveryLogService {

    private final MailDeliveryRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long claim(User user, MailType mailType, String deliveryKey) {
        MailDelivery existing = repository.findByDeliveryKey(deliveryKey).orElse(null);
        if (existing != null) {
            if (!existing.canRetry()) {
                return null;
            }
            existing.retry();
            return existing.getId();
        }
        return repository.saveAndFlush(MailDelivery.builder()
                .user(user)
                .mailType(mailType)
                .deliveryKey(deliveryKey)
                .recipientEmail(user.getEmail())
                .build()).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Long deliveryId) {
        repository.findById(deliveryId).ifPresent(delivery -> delivery.markSent(LocalDateTime.now()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long deliveryId, Throwable throwable) {
        repository.findById(deliveryId).ifPresent(delivery -> delivery.markFailed(rootMessage(throwable)));
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
