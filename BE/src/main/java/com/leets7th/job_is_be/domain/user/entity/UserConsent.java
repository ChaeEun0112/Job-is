package com.leets7th.job_is_be.domain.user.entity;

import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_consents",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_consents_user", columnNames = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConsent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "terms_agreed", nullable = false)
    private boolean termsAgreed;

    @Column(name = "privacy_agreed", nullable = false)
    private boolean privacyAgreed;

    @Column(name = "age_over_14_agreed", nullable = false)
    private boolean ageOver14Agreed;

    @Column(name = "marketing_agreed", nullable = false)
    private boolean marketingAgreed;

    @Column(name = "agreed_at", nullable = false)
    private OffsetDateTime agreedAt;

    @Builder
    public UserConsent(
            User user,
            boolean termsAgreed,
            boolean privacyAgreed,
            boolean ageOver14Agreed,
            boolean marketingAgreed,
            OffsetDateTime agreedAt
    ) {
        this.user = user;
        update(termsAgreed, privacyAgreed, ageOver14Agreed, marketingAgreed, agreedAt);
    }

    public void update(
            boolean termsAgreed,
            boolean privacyAgreed,
            boolean ageOver14Agreed,
            boolean marketingAgreed,
            OffsetDateTime agreedAt
    ) {
        this.termsAgreed = termsAgreed;
        this.privacyAgreed = privacyAgreed;
        this.ageOver14Agreed = ageOver14Agreed;
        this.marketingAgreed = marketingAgreed;
        this.agreedAt = agreedAt;
    }
}
