package com.leets7th.job_is_be.domain.user.entity;

import com.leets7th.job_is_be.domain.user.enums.ResumeCategory;
import com.leets7th.job_is_be.domain.user.enums.ResumeFileFormat;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 이력서/자소서 파일 (S3 Presigned URL 업로드, 유형별 1슬롯)
 */
@Entity
@Table(
        name = "resumes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_resumes_user_category", columnNames = {"user_id", "category"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ResumeCategory category;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 10)
    private ResumeFileFormat fileFormat;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Builder
    public Resume(User user, ResumeCategory category, String fileName, ResumeFileFormat fileFormat, String s3Key, OffsetDateTime uploadedAt) {
        this.user = user;
        this.category = category;
        this.fileName = fileName;
        this.fileFormat = fileFormat;
        this.s3Key = s3Key;
        this.uploadedAt = uploadedAt;
    }

    /**
     * 같은 유형(user+category) 재업로드 시 기존 row를 새 파일 정보로 갱신 (S3 오브젝트 키는 고정이라 그대로 유지)
     */
    public void replace(String fileName, ResumeFileFormat fileFormat, OffsetDateTime uploadedAt) {
        this.fileName = fileName;
        this.fileFormat = fileFormat;
        this.uploadedAt = uploadedAt;
    }
}
