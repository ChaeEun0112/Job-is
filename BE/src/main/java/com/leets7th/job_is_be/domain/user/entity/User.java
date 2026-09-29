package com.leets7th.job_is_be.domain.user.entity;

import com.leets7th.job_is_be.domain.user.enums.Role;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;


@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_social", columnNames = {"social_id", "social_type"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "social_id", nullable = false, length = 255)
    private String socialId; // 구글/카카오에서 주는 고유 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    private SocialType socialType;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "withdrawn_at")
    private OffsetDateTime withdrawnAt;

    @Builder
    public User(String socialId, SocialType socialType, String email) {
        this.socialId = socialId;
        this.socialType = socialType;
        this.email = email;
        this.status = UserStatus.ACTIVE;
        this.role = Role.USER;
    }

    public void withdraw(OffsetDateTime withdrawnAt) {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = withdrawnAt;
    }

    public void restore() {
        this.status = UserStatus.ACTIVE;
        this.withdrawnAt = null;
    }
}
