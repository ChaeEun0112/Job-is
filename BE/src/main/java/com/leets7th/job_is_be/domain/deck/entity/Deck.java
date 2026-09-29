package com.leets7th.job_is_be.domain.deck.entity;

import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 사용자별 일일 추천 덱 헤더
 */
@Entity
@Table(
        name = "decks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_decks_user_date",
                columnNames = {"user_id", "deck_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deck extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "deck_date", nullable = false)
    private LocalDate deckDate;

    @Column(name = "empty_reason", length = 30)
    private String emptyReason; // no_candidates, onboarding_incomplete, pre_slot (REC-07)

    @Column(name = "first_opened_at")
    private OffsetDateTime firstOpenedAt; // 재방문 판별 기준 (REC-06)

    @Column(name = "completed_at")
    private OffsetDateTime completedAt; // 전량 처리 완료 시점 (REC-05)

    @Builder
    public Deck(User user, LocalDate deckDate) {
        this.user = user;
        this.deckDate = deckDate;
    }

    public void markFirstOpened(OffsetDateTime now) {
        if (this.firstOpenedAt == null) {
            this.firstOpenedAt = now;
        }
    }

    public void complete(OffsetDateTime now) {
        this.completedAt = now;
    }

    public void markEmpty(String reason) {
        this.emptyReason = reason;
    }
}
