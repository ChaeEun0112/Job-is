package com.leets7th.job_is_be.domain.user.entity;


import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import com.leets7th.job_is_be.domain.user.enums.OnboardingStep;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 사용자 프로필. User와 1:1 관계
 */
@Entity
@Table(name = "user_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_profiles_user", columnNames = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_level", length = 20)
    private CareerLevel careerLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_step", length = 20)
    private OnboardingStep onboardingStep;

    @Column(name = "preference_note", length = 500)
    private String preferenceNote; // 선호 조건 텍스트 (재택 우선 등)

    @Column(name = "exclude_keywords", length = 500)
    private String excludeKeywords; // 제외 조건 (야근 없는 곳 등)

    @Column(name = "tech_stack", length = 500)
    private String techStack; // 보유/선호 기술 스택, comma-separated

    /**
     * 원격 포함 희망 여부(PRO-01 희망 지역 필드의 별도 토글).
     * 매칭 하드필터에서 "도시 일치 OR 원격"으로 쓰인다(03-matching-contract §4.1).
     * 기존 행 때문에 DB 기본값을 명시한다.
     */
    @Column(name = "remote_ok", nullable = false, columnDefinition = "boolean not null default false")
    private boolean remoteOk;

    @Column(name = "personality_tags", length = 500)
    private String personalityTags;

    @Column(name = "is_job_test_completed", nullable = false)
    private boolean jobTestCompleted;

    @Column(name = "job_test_completed_at")
    private OffsetDateTime jobTestCompletedAt;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Column(name = "onboarding_completed_at")
    private OffsetDateTime onboardingCompletedAt;

    @Builder
    public UserProfile(User user, CareerLevel careerLevel, OnboardingStep onboardingStep,
                       String preferenceNote, String excludeKeywords, String techStack) {
        this.user = user;
        this.careerLevel = careerLevel;
        this.onboardingStep = onboardingStep;
        this.preferenceNote = preferenceNote;
        this.excludeKeywords = excludeKeywords;
        this.techStack = techStack;
        this.jobTestCompleted = false;
        this.onboardingCompleted = false;
    }

    public void completeOnboarding(OffsetDateTime now) {
        this.onboardingCompleted = true;
        this.onboardingCompletedAt = now;
    }

    public void updateDraft(
            CareerLevel careerLevel,
            OnboardingStep onboardingStep,
            String preferenceNote,
            String excludeKeywords,
            String techStack,
            boolean remoteOk
    ) {
        this.careerLevel = careerLevel;
        this.onboardingStep = onboardingStep;
        this.preferenceNote = preferenceNote;
        this.excludeKeywords = excludeKeywords;
        this.techStack = techStack;
        this.remoteOk = remoteOk;
    }

    public void updateProfile(
            CareerLevel careerLevel,
            String preferenceNote,
            String excludeKeywords,
            String techStack,
            boolean remoteOk
    ) {
        this.careerLevel = careerLevel;
        this.preferenceNote = preferenceNote;
        this.excludeKeywords = excludeKeywords;
        this.techStack = techStack;
        this.remoteOk = remoteOk;
    }

    public void moveOnboardingStep(OnboardingStep onboardingStep) {
        this.onboardingStep = onboardingStep;
    }

    public void completeJobTest(OffsetDateTime now) {
        this.jobTestCompleted = true;
        this.jobTestCompletedAt = now;
    }

    public void applyPersonalityTags(String personalityTags, OffsetDateTime now) {
        this.personalityTags = personalityTags;
        completeJobTest(now);
    }
}
