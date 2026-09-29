package com.leets7th.job_is_be.domain.notification.entity;

import com.leets7th.job_is_be.domain.notification.enums.UnsubscribeReason;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수신거부(구독 해지) 사유 제출 기록
 */
@Entity
@Table(name = "unsubscribe_feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnsubscribeFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private UnsubscribeReason reason;

    @Column(length = 200)
    private String comment; // reason=OTHER일 때 자유 코멘트 (선택)

    @Builder
    public UnsubscribeFeedback(User user, UnsubscribeReason reason, String comment) {
        this.user = user;
        this.reason = reason;
        this.comment = comment;
    }
}
