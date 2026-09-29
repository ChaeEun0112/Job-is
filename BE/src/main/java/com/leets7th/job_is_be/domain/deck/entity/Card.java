package com.leets7th.job_is_be.domain.deck.entity;


import com.leets7th.job_is_be.domain.deck.enums.DeckItemStatus;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 덱 안의 개별 카드
@Entity
@Table(name = "cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(nullable = false)
    private Integer position; // 덱 내 카드 순서

    @Column(name = "fit_score", precision = 5, scale = 2)
    private BigDecimal fitScore; // 적합도

    @Column(length = 300)
    private String reason; // 추천 이유 한 줄

    @Column(length = 500)
    private String summary; // 한눈에 요약

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeckItemStatus status;

    @Column(name = "reason_submitted", nullable = false)
    private boolean reasonSubmitted; // 관심없음 사유 제출 여부 (DET-03), 관심없음 사이클마다 초기화

    @Version
    private Long version; // 동시 관심없음/사유제출 요청 시 상태 검사-전환을 원자적으로 보호하기 위한 낙관적 락

    @Builder
    public Card(Deck deck, Job job, Integer position, BigDecimal fitScore, String reason, String summary) {
        this.deck = deck;
        this.job = job;
        this.position = position;
        this.fitScore = fitScore;
        this.reason = reason;
        this.summary = summary;
        this.status = DeckItemStatus.PENDING;
    }

    public void save() {
        this.status = DeckItemStatus.SAVED;
    }

    public void dismiss() {
        this.status = DeckItemStatus.DISMISSED;
        this.reasonSubmitted = false;
    }

    public void undismiss() {
        this.status = DeckItemStatus.PENDING;
        this.reasonSubmitted = false;
    }

    public void markReasonSubmitted() {
        this.reasonSubmitted = true;
    }
}
