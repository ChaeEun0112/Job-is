package com.leets7th.job_is_be.domain.job.entity;

import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "tech_stacks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tech_stacks_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_tech_stacks_normalized_name", columnNames = "normalized_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechStack extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 100)
    private String normalizedName;

    @Builder
    public TechStack(String name, String normalizedName) {
        this.name = name;
        this.normalizedName = normalizedName;
    }
}
