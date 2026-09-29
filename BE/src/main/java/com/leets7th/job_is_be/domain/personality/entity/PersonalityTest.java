package com.leets7th.job_is_be.domain.personality.entity;

import com.leets7th.job_is_be.domain.personality.enums.PersonalityResultType;
import com.leets7th.job_is_be.domain.personality.enums.PersonalityTestSource;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 직무 성향 퀴즈/테스트 결과
 */
@Entity
@Table(name = "personality_tests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalityTest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PersonalityTestSource source;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "result_tags", length = 500)
    private String resultTags; // 결과 태그(JSON/CSV), 추천 선호 태그 시드

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", length = 2)
    private PersonalityResultType resultType;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Builder
    public PersonalityTest(
            User user,
            PersonalityTestSource source,
            boolean completed,
            String resultTags,
            PersonalityResultType resultType,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt
    ) {
        this.user = user;
        this.source = source;
        this.completed = completed;
        this.resultTags = resultTags;
        this.resultType = resultType;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public void complete(
            PersonalityResultType resultType,
            String resultTags,
            OffsetDateTime now
    ) {
        this.completed = true;
        this.resultType = resultType;
        this.resultTags = resultTags;
        this.completedAt = now;
    }
}
