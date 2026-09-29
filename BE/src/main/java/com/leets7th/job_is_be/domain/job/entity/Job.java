package com.leets7th.job_is_be.domain.job.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 채용 공고
 */
@Entity
@Table(
        name = "jobs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_jobs_source_external_id",
                columnNames = {"source", "external_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id")
    private JobCategory jobCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @JsonAlias({"job_title", "position", "title"})
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "career_level", length = 30)
    private String careerLevel; // 신입, 신입-주니어, 경력무관 등 (외부 원문 표기 그대로)

    @Column(name = "employment_type", length = 30)
    private String employmentType; // 정규직 등

    @Column(name = "remote_available")
    private Boolean remoteAvailable;

    @Column(name = "salary_disclosed")
    private Boolean salaryDisclosed; // §11 연봉 원문 확인 문구 처리 기준

    @Column(length = 50)
    private String source; // 원티드 등 원문 출처

    @Column(name = "external_id")
    private Long externalId; // 원문 출처 내 공고 id. (source, externalId)로 크롤링 동기화 시 upsert 매칭

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(name = "posted_at")
    private OffsetDateTime postedAt;

    @Column(name = "deadline_at")
    private OffsetDateTime deadlineAt; // NULL이면 "상시" 표시 (§4.4)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "editor_note", length = 1000)
    private String editorNote; // Editor's Note (DET-01)

    @Column(name = "location_full", columnDefinition = "TEXT")
    private String locationFull;

    @Column(columnDefinition = "TEXT")
    private String intro; // 팀/회사 소개

    @Column(name = "main_tasks", columnDefinition = "TEXT")
    private String mainTasks; // 담당업무

    @Column(columnDefinition = "TEXT")
    private String requirements; // 자격요건

    @Column(name = "preferred_points", columnDefinition = "TEXT")
    private String preferredPoints; // 우대사항

    @Column(columnDefinition = "TEXT")
    private String benefits; // 복지/문화

    @Column(name = "career_min")
    private Integer careerMin; // 경력 최소 연수

    @Column(name = "career_max")
    private Integer careerMax; // 경력 최대 연수

    @Column(name = "reward_total", columnDefinition = "TEXT")
    private String rewardTotal; // 추천 보상금

    @JsonProperty("thumbnail_url")
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl; // 카드 썸네일 URL

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "categories", columnDefinition = "TEXT")
    private String categories;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skill_tags")
    private List<String> skillTags;

    @Column(name = "skills_inferred")
    private Boolean skillsInferred;

    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embedding;  // pgvector 형식 (JSON 배열 문자열로 저장)

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Column(name = "location_district", length = 100)
    private String locationDistrict;

    @Column(name = "is_newbie")
    private Boolean isNewbie;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "category_child")
    private List<String> categoryChild;

    @Builder
    public Job(Company company, JobCategory jobCategory, Region region, String title,
               String careerLevel, String employmentType, Boolean remoteAvailable,
               Boolean salaryDisclosed, String source, Long externalId, String sourceUrl,
               OffsetDateTime postedAt, OffsetDateTime deadlineAt, String editorNote,
               String locationFull, String intro, String mainTasks, String requirements,
               String preferredPoints, String benefits, Integer careerMin, Integer careerMax,
               String rewardTotal, String thumbnailUrl, String skills, String categories,
               List<String> skillTags, Boolean skillsInferred, String embedding,
               List<String> categoryChild, String locationCity, String locationDistrict,
               Boolean isNewbie) {
        this.company = company;
        this.jobCategory = jobCategory;
        this.region = region;
        this.title = title;
        this.careerLevel = careerLevel;
        this.employmentType = employmentType;
        this.remoteAvailable = remoteAvailable;
        this.salaryDisclosed = salaryDisclosed;
        this.source = source;
        this.externalId = externalId;
        this.sourceUrl = sourceUrl;
        this.postedAt = postedAt;
        this.deadlineAt = deadlineAt;
        this.editorNote = editorNote;
        this.locationFull = locationFull;
        this.intro = intro;
        this.mainTasks = mainTasks;
        this.requirements = requirements;
        this.preferredPoints = preferredPoints;
        this.benefits = benefits;
        this.careerMin = careerMin;
        this.careerMax = careerMax;
        this.rewardTotal = rewardTotal;
        this.thumbnailUrl = thumbnailUrl;
        this.skills = skills;
        this.categories = categories;
        this.skillTags = skillTags;
        this.skillsInferred = skillsInferred;
        this.embedding = embedding;
        this.categoryChild = categoryChild;
        this.locationCity = locationCity;
        this.locationDistrict = locationDistrict;
        this.isNewbie = isNewbie;
        this.status = JobStatus.ACTIVE;
    }

    public void expire() {
        this.status = JobStatus.EXPIRED;
    }

    public void markRemoved() {
        this.status = JobStatus.REMOVED;
    }

    public boolean isOpenEnded() {
        return this.deadlineAt == null;
    }

    // 크롤링 재수집 시 (source, externalId)로 매칭된 기존 공고에 최신 원문 내용을 반영
    public void syncFrom(Company company, String title, String careerLevel, String employmentType,
                         Boolean remoteAvailable, String sourceUrl,
                         OffsetDateTime postedAt, OffsetDateTime deadlineAt, JobStatus status,
                         String locationFull, String mainTasks, String requirements,
                         String preferredPoints, List<String> skillTags, Boolean skillsInferred, String embedding,
                         List<String> categoryChild, String locationCity, String locationDistrict,
                         Boolean isNewbie, String thumbnailUrl) {
        if (embedding != null) {
            this.embedding = embedding;  // ← null이 아닐 때만 업데이트
        }
        this.company = company;
        this.title = title;
        this.careerLevel = careerLevel;
        this.employmentType = employmentType;
        this.remoteAvailable = remoteAvailable;
        this.sourceUrl = sourceUrl;
        this.postedAt = postedAt;
        this.deadlineAt = deadlineAt;
        this.status = status;
        this.locationFull = locationFull;
        this.mainTasks = mainTasks;
        this.requirements = requirements;
        this.preferredPoints = preferredPoints;
        this.skillTags = skillTags;
        this.skillsInferred = skillsInferred;
        this.categoryChild = categoryChild;
        this.locationCity = locationCity;
        this.locationDistrict = locationDistrict;
        this.isNewbie = isNewbie;
        this.thumbnailUrl = thumbnailUrl;
    }
}
