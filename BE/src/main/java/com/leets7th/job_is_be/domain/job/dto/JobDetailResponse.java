package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JobDetailResponse (
        Long id,
        String companyName,
        String position,
        String careerLevel,
        String employmentType,
        Boolean remoteAvailable,
        String sourceUrl,
        OffsetDateTime postedAt,
        OffsetDateTime dueTime,
        JobStatus status,
        String editorNote,

        String intro,
        String mainTasks,
        String requirements,
        String preferredPoints,
        String benefits,

        Integer employeeCount,
        String companyType,
        String industry,
        String stockStatus,

        List<String> skillTags,
        String locationFull,
        JobMatchingResponse matching
) {
    public static JobDetailResponse from(Job job) {
        return from(job, null);
    }

    public static JobDetailResponse from(Job job, JobMatchingResponse matching) {

        var company = job.getCompany();

        return JobDetailResponse.builder()
                .matching(matching)
                .id(job.getId())
                .companyName(company != null ? company.getName() : null)
                .position(job.getTitle())
                .careerLevel(job.getCareerLevel())
                .employmentType(job.getEmploymentType())
                .remoteAvailable(job.getRemoteAvailable())
                .sourceUrl(job.getSourceUrl())
                .postedAt(job.getPostedAt())
                .dueTime(job.getDeadlineAt())
                .status(job.getStatus())
                .editorNote(job.getEditorNote())
                .intro(job.getIntro())
                .mainTasks(job.getMainTasks())
                .requirements(job.getRequirements())
                .preferredPoints(job.getPreferredPoints())
                .benefits(job.getBenefits())
                .employeeCount(company != null ? company.getEmployeeCount() : null)
                .companyType(company != null ? company.getCompanyType() : null)
                .industry(company != null ? company.getIndustry() : null)
                .stockStatus(company != null ? company.getStockStatus() : null)
                .skillTags(job.getSkillTags())
                .locationFull(job.getLocationFull())
                .editorNote(job.getEditorNote())
                .build();
    }
}
