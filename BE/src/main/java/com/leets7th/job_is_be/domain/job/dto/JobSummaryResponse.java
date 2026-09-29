package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.Job;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JobSummaryResponse(
        Long id,
        String companyName,
        String position,
        String careerLevel,
        String employmentType,
        Boolean remoteAvailable,
        OffsetDateTime dueTime,
        String thumbnailUrl,
        List<String> skillTags,

        @Schema(description = "카드 메타라인용 지역 시/도", example = "서울")
        String locationCity,

        @Schema(description = "카드 메타라인용 지역 구/군", example = "강남구")
        String locationDistrict,

        @Schema(description = "적합도 배지 점수(0~100). 산출 불가 시 null — 프런트는 배지를 생략한다.")
        Integer fitScore
) {
    public static JobSummaryResponse from(Job job) {
        return from(job, null);
    }

    public static JobSummaryResponse from(Job job, Integer fitScore) {
        // 이미 List<String>이므로 별도 스트림 변환 없이 그대로 할당 가능
        List<String> tags = job.getSkillTags() != null ? job.getSkillTags() : List.of();

        return JobSummaryResponse.builder()
                .id(job.getId())
                .companyName(job.getCompany() != null ? job.getCompany().getName() : null)
                .position(job.getTitle())
                .careerLevel(job.getCareerLevel())
                .employmentType(job.getEmploymentType())
                .remoteAvailable(job.getRemoteAvailable())
                .dueTime(job.getDeadlineAt())
                .thumbnailUrl(job.getThumbnailUrl())
                .skillTags(tags)
                .locationCity(job.getLocationCity())
                .locationDistrict(job.getLocationDistrict())
                .fitScore(fitScore)
                .build();
    }
}
