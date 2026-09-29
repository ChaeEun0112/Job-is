package com.leets7th.job_is_be.domain.user.entity;

import com.leets7th.job_is_be.domain.job.entity.TechStack;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.Entity;
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

@Entity
@Table(
        name = "user_tech_stacks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_tech_stacks_user_stack",
                columnNames = {"user_id", "tech_stack_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTechStack extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", nullable = false)
    private TechStack techStack;

    @Builder
    public UserTechStack(User user, TechStack techStack) {
        this.user = user;
        this.techStack = techStack;
    }
}
