package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CrawledCompanyDto(
        String name,        // 기업명
        @JsonProperty("normalized_name") String normalizedName,
        String logoUrl,     // 로고 이미지 URL
        String description, // 기업 한 줄 소개 또는 설명
        @JsonProperty("registration_number") String registrationNumber,
        @JsonProperty("wanted_company_id") Long wantedCompanyId,
        @JsonProperty("jobkorea_gno_ref") Long jobkoreaGnoRef,
        @JsonProperty("employee_count") Integer employeeCount,
        @JsonProperty("company_type") String companyType,
        String industry,
        @JsonProperty("stock_status") String stockStatus,
        @JsonProperty("hq_address") String hqAddress,
        @JsonProperty("enrichment_status") String enrichmentStatus,
        @JsonProperty("name_match") Boolean nameMatch,
        @JsonProperty("rejected_name") String rejectedName,
        @JsonProperty("raw_jobkorea") JsonNode rawJobkorea
) {}
