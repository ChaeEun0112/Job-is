package com.leets7th.job_is_be.domain.deck.entity;

import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_actions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;

    @Column(name = "reason_code", length = 30)
    private String reasonCode; // 관심없음 사유 (DET-03): 직무불일치 등

    @Column(length = 200)
    private String comment; // 관심없음 사유 자유 코멘트 (DET-03), 최대 200자

    @Column(name = "source_screen", length = 20)
    private String sourceScreen; // 이벤트 발생 화면 ID (REC-03, EXP-03 등)

    @Builder
    public UserAction(User user, Job job, ActionType actionType, String reasonCode,
                      String comment, String sourceScreen) {
        this.user = user;
        this.job = job;
        this.actionType = actionType;
        this.reasonCode = reasonCode;
        this.comment = comment;
        this.sourceScreen = sourceScreen;
    }
}
