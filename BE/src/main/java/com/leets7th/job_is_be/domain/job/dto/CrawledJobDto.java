package com.leets7th.job_is_be.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CrawledJobDto(
        @JsonProperty("position") String title,
        @JsonProperty("source_url") String detailUrl,
        @JsonProperty("reward_total") String reward,
        @JsonProperty("skill_tags") List<String> skills,
        CrawledCompanyDto company,
        @JsonProperty("category_child") List<String> categoryChild,
        @JsonProperty("category_parent") String categoryName,
        String intro,
        @JsonProperty("main_tasks") String mainTasks,
        String requirements,
        @JsonProperty("preferred_points") String preferredPoints,
        String benefits,
        @JsonProperty("career_min") Integer careerMin,
        @JsonProperty("career_max") Integer careerMax,
        @JsonProperty("is_newbie") Boolean isNewbie,
        String regionName,
        String source,
        @JsonProperty("external_id") Long externalId,
        @JsonProperty("employment_type") String employmentType,
        @JsonProperty("is_remote") boolean remoteAvailable,
        // 크롤러 원문은 confirm_time="yyyy-MM-dd"(날짜만), due_time="yyyy-MM-dd'T'HH:mm:ss"(오프셋 없음)로 내려온다.
        @JsonProperty("confirm_time") LocalDate postedAt,
        @JsonProperty("due_time") LocalDateTime deadlineAt,
        @JsonProperty("location_city") String locationCity,
        @JsonProperty("location_district") String locationDistrict,
        @JsonProperty("location_full") String locationFull,
        @JsonProperty("thumbnail_url") String thumbnailUrl
) {
    /**
     * 크롤러가 "경력" 요약 문자열을 따로 주지 않아, career_min/career_max/is_newbie로부터 계산한다.
     */
    public String careerLevel() {
        if (Boolean.TRUE.equals(isNewbie) && careerMin == null) {
            return "신입";
        }
        if (careerMin != null && careerMax != null) {
            return careerMin + "~" + careerMax + "년";
        }
        if (careerMin != null) {
            return careerMin + "년 이상";
        }
        return "경력무관";
    }
}
