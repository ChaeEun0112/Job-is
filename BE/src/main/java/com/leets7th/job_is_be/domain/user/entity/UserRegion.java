package com.leets7th.job_is_be.domain.user.entity;


import com.leets7th.job_is_be.domain.job.entity.Region;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 희망 지역
 */
@Entity
@Table(
        name = "user_regions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_regions_user_region",
                columnNames = {"user_id", "region_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Builder
    public UserRegion(User user, Region region) {
        this.user = user;
        this.region = region;
    }
}
