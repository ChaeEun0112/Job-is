package com.leets7th.job_is_be.domain.job.entity;

import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;


@Entity
@Table(
        name = "saved_jobs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_saved_jobs_user_job",
                columnNames = {"user_id", "job_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "saved_at", nullable = false)
    private OffsetDateTime savedAt;

    // 지원함 등 자기보고 상태 — 추후 지원 현황 트래킹에 활용
    @Column(name = "self_reported_status", length = 20)
    private String selfReportedStatus;

    @Builder
    public SavedJob(User user, Job job, OffsetDateTime savedAt) {
        this.user = user;
        this.job = job;
        this.savedAt = savedAt;
    }

    public void markSelfReportedStatus(String status) {
        this.selfReportedStatus = status;
    }
}

