package com.leets7th.job_is_be.domain.job.entity;


import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 직무/직군 마스터 코드. user, job 양쪽 컨텍스트가 공유하는 Shared Kernel.
 */
@Entity
@Table(name = "job_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 예: 백엔드 엔지니어

    @Column(name = "group_name", length = 100)
    private String groupName; // 상위 직군, 예: IT/개발

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public JobCategory(String name, String groupName, Integer sortOrder) {
        this.name = name;
        this.groupName = groupName;
        this.sortOrder = sortOrder;
    }
}
