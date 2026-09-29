package com.leets7th.job_is_be.domain.user.entity;


import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 관심 직무/직군
 */
@Entity
@Table(
        name = "user_job_categories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_job_categories_user_category",
                columnNames = {"user_id", "job_category_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJobCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory jobCategory;

    @Column(name = "is_primary", nullable = false)
    private boolean primary; // 대표 관심 직무 여부

    @Builder
    public UserJobCategory(User user, JobCategory jobCategory, boolean primary) {
        this.user = user;
        this.jobCategory = jobCategory;
        this.primary = primary;
    }
}
