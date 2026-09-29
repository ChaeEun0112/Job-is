package com.leets7th.job_is_be.domain.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

// 원티드 공고 원문(크롤러 적재). 앱은 읽기 전용으로만 사용, jobs 테이블로의 동기화는 JobPostingConverter가 담당
@Entity
@Table(name = "job_postings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String source;

    @Column(name = "external_id")
    private Long externalId;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(columnDefinition = "TEXT")
    private String position;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(name = "main_tasks", columnDefinition = "TEXT")
    private String mainTasks;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "preferred_points", columnDefinition = "TEXT")
    private String preferredPoints;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "category_parent", length = 50)
    private String categoryParent;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "category_child")
    private List<String> categoryChild;

    @Column(name = "career_min")
    private Integer careerMin;

    @Column(name = "career_max")
    private Integer careerMax;

    @Column(name = "is_newbie")
    private Boolean isNewbie;

    @Column(name = "is_expert")
    private Boolean isExpert;

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(name = "location_country", length = 100)
    private String locationCountry;

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Column(name = "location_district", length = 100)
    private String locationDistrict;

    @Column(name = "location_full", columnDefinition = "TEXT")
    private String locationFull;

    @Column(name = "geo_lat")
    private Double geoLat;

    @Column(name = "geo_lng")
    private Double geoLng;

    @Column(name = "is_remote")
    private Boolean isRemote;

    @Column(name = "due_time")
    private OffsetDateTime dueTime;

    @Column(name = "confirm_time")
    private OffsetDateTime confirmTime;

    @Column(length = 20)
    private String status;

    @Column(name = "hire_rounds", columnDefinition = "TEXT")
    private String hireRounds;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skill_tags")
    private List<String> skillTags;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skill_tag_ids")
    private List<Integer> skillTagIds;

    @Column(name = "skills_inferred")
    private Boolean skillsInferred;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "image_urls")
    private List<String> imageUrls;

    @Column(name = "reward_total", length = 100)
    private String rewardTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    private String raw;

    @Column(name = "collected_at")
    private OffsetDateTime collectedAt;

    @Builder
    public JobPosting(String source, Long externalId, String sourceUrl, Company company, String position,
                       String intro, String mainTasks, String requirements, String preferredPoints,
                       String benefits, String categoryParent, List<String> categoryChild, Integer careerMin,
                       Integer careerMax, Boolean isNewbie, Boolean isExpert, String employmentType,
                       String locationCountry, String locationCity, String locationDistrict, String locationFull,
                       Double geoLat, Double geoLng, Boolean isRemote, OffsetDateTime dueTime,
                       OffsetDateTime confirmTime, String status, String hireRounds, List<String> skillTags,
                       List<Integer> skillTagIds, Boolean skillsInferred, String thumbnailUrl,
                       List<String> imageUrls, String rewardTotal, String raw, OffsetDateTime collectedAt) {
        this.source = source;
        this.externalId = externalId;
        this.sourceUrl = sourceUrl;
        this.company = company;
        this.position = position;
        this.intro = intro;
        this.mainTasks = mainTasks;
        this.requirements = requirements;
        this.preferredPoints = preferredPoints;
        this.benefits = benefits;
        this.categoryParent = categoryParent;
        this.categoryChild = categoryChild;
        this.careerMin = careerMin;
        this.careerMax = careerMax;
        this.isNewbie = isNewbie;
        this.isExpert = isExpert;
        this.employmentType = employmentType;
        this.locationCountry = locationCountry;
        this.locationCity = locationCity;
        this.locationDistrict = locationDistrict;
        this.locationFull = locationFull;
        this.geoLat = geoLat;
        this.geoLng = geoLng;
        this.isRemote = isRemote;
        this.dueTime = dueTime;
        this.confirmTime = confirmTime;
        this.status = status;
        this.hireRounds = hireRounds;
        this.skillTags = skillTags;
        this.skillTagIds = skillTagIds;
        this.skillsInferred = skillsInferred;
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrls = imageUrls;
        this.rewardTotal = rewardTotal;
        this.raw = raw;
        this.collectedAt = collectedAt;
    }
}
