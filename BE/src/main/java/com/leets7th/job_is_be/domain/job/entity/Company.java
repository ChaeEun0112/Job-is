package com.leets7th.job_is_be.domain.job.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

// 기업 정보. 크롤러(원티드+잡코리아 메타)가 이 테이블을 직접 적재/갱신하므로 앱은 읽기 전용으로만 사용
@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String name;

    @Column(name = "normalized_name", length = 200)
    private String normalizedName;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "wanted_company_id")
    private Long wantedCompanyId;

    @Column(name = "jobkorea_gno_ref")
    private Long jobkoreaGnoRef;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "company_type", length = 50)
    private String companyType;

    @Column(length = 100)
    private String industry;

    @Column(name = "stock_status", length = 50)
    private String stockStatus;

    @Column(name = "hq_address", length = 500)
    private String hqAddress;

    @Column(length = 500)
    private String homepage;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(columnDefinition = "TEXT") // erd 규격 많이 어기는데, 1000자 넘기면 자르는 걸로 바꿀까요?
    private String description;

    @Column(name = "enrichment_status", length = 50)
    private String enrichmentStatus;

    @Column(name = "name_match")
    private Boolean nameMatch;

    @Column(name = "rejected_name", length = 200)
    private String rejectedName;

    @Column(name = "enriched_at")
    private OffsetDateTime enrichedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_jobkorea")
    private JsonNode rawJobkorea;

    @Builder
    public Company(String name, String normalizedName, String registrationNumber,
                   Long wantedCompanyId, Long jobkoreaGnoRef, Integer employeeCount, String companyType,
                   String industry, String stockStatus, String hqAddress, String homepage,
                   String description, String enrichmentStatus, Boolean nameMatch, String rejectedName,
                   OffsetDateTime enrichedAt, JsonNode rawJobkorea, String source, String logoUrl) {
        this.name = name;
        this.normalizedName = normalizedName;
        this.registrationNumber = registrationNumber;
        this.wantedCompanyId = wantedCompanyId;
        this.jobkoreaGnoRef = jobkoreaGnoRef;
        this.employeeCount = employeeCount;
        this.companyType = companyType;
        this.industry = industry;
        this.stockStatus = stockStatus;
        this.hqAddress = hqAddress;
        this.homepage = homepage;
        this.description = description;
        this.enrichmentStatus = enrichmentStatus;
        this.nameMatch = nameMatch;
        this.rejectedName = rejectedName;
        this.enrichedAt = enrichedAt;
        this.rawJobkorea = rawJobkorea;
        this.source = source;
        this.logoUrl = logoUrl;
        this.name = name;
        this.normalizedName = normalizedName;
        this.registrationNumber = registrationNumber;
        this.companyType = companyType;
        this.industry = industry;
        this.stockStatus = stockStatus;
        this.hqAddress = hqAddress;
        this.homepage = homepage;
        this.description = description;
        this.enrichmentStatus = enrichmentStatus;
        this.rejectedName = rejectedName;
        this.source = source;
        this.logoUrl = logoUrl;
    }

    // 잡코리아 메타 보강 결과를 기존 회사 레코드에 반영한다(이전 크롤링에서 not_found/name_mismatch였던 회사가 이후 enriched로 갱신되는 경우 등).
    public void enrich(Long jobkoreaGnoRef, Integer employeeCount, String companyType, String industry,
                        String stockStatus, String hqAddress, String enrichmentStatus, Boolean nameMatch,
                        String rejectedName, JsonNode rawJobkorea, OffsetDateTime enrichedAt) {
        this.jobkoreaGnoRef = jobkoreaGnoRef;
        this.employeeCount = employeeCount;
        this.companyType = companyType;
        this.industry = industry;
        this.stockStatus = stockStatus;
        this.hqAddress = hqAddress;
        this.enrichmentStatus = enrichmentStatus;
        this.nameMatch = nameMatch;
        this.rejectedName = rejectedName;
        this.rawJobkorea = rawJobkorea;
        this.enrichedAt = enrichedAt;
    }
}
