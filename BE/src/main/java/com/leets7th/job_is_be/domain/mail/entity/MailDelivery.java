package com.leets7th.job_is_be.domain.mail.entity;

import com.leets7th.job_is_be.domain.mail.enums.MailDeliveryStatus;
import com.leets7th.job_is_be.domain.mail.enums.MailType;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "email_send_logs",
        uniqueConstraints = @UniqueConstraint(name = "uk_email_send_logs_delivery_key", columnNames = "delivery_key")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailDelivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "mail_type", nullable = false, length = 30)
    private MailType mailType;

    @Column(name = "delivery_key", nullable = false, length = 100)
    private String deliveryKey;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MailDeliveryStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Builder
    public MailDelivery(User user, MailType mailType, String deliveryKey, String recipientEmail) {
        this.user = user;
        this.mailType = mailType;
        this.deliveryKey = deliveryKey;
        this.recipientEmail = recipientEmail;
        this.status = MailDeliveryStatus.PENDING;
    }

    public boolean canRetry() {
        return status == MailDeliveryStatus.FAILED;
    }

    public void retry() {
        status = MailDeliveryStatus.PENDING;
        failureReason = null;
    }

    public void markSent(LocalDateTime now) {
        status = MailDeliveryStatus.SENT;
        sentAt = now;
        failureReason = null;
    }

    public void markFailed(String reason) {
        status = MailDeliveryStatus.FAILED;
        failureReason = reason == null ? null : reason.substring(0, Math.min(reason.length(), 500));
    }
}
