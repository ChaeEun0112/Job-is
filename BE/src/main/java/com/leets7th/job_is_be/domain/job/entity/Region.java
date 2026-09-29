package com.leets7th.job_is_be.domain.job.entity;


import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지역 마스터 코드. parent로 자기 자신을 참조해 시/도 - 구/군 계층을 표현.
 */
@Entity
@Table(name = "regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 예: 서울 강남

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Region parent; // 상위 지역(시/도), 자기참조

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public Region(String name, Region parent, Integer sortOrder) {
        this.name = name;
        this.parent = parent;
        this.sortOrder = sortOrder;
    }
}
