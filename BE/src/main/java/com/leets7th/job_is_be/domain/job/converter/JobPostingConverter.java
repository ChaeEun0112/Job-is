package com.leets7th.job_is_be.domain.job.converter;

import com.leets7th.job_is_be.domain.job.dto.JobDetailResponse;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobPosting;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

// job_postings 원문(JobPosting) -> 서빙용 Job 매핑. jobCategory/region은 마스터 데이터 정규화가
// 별도로 필요해 이번 스코프에서는 채우지 않음
@Component
public class JobPostingConverter {

    public Job createFrom(JobPosting posting) {
        Job job = Job.builder()
                .company(posting.getCompany())
                .title(posting.getPosition())
                .careerLevel(resolveCareerLevel(posting))
                .employmentType(posting.getEmploymentType())
                .remoteAvailable(Boolean.TRUE.equals(posting.getIsRemote()))
                .salaryDisclosed(false)
                .source(posting.getSource())
                .externalId(posting.getExternalId())
                .sourceUrl(posting.getSourceUrl())
                .postedAt(posting.getConfirmTime())
                .deadlineAt(posting.getDueTime())
                .locationFull(posting.getLocationFull())
                .mainTasks(posting.getMainTasks())
                .requirements(posting.getRequirements())
                .preferredPoints(posting.getPreferredPoints())
                .skillTags(posting.getSkillTags())
                .skillsInferred(posting.getSkillsInferred())
                .categoryChild(posting.getCategoryChild())
                .locationCity(posting.getLocationCity())
                .locationDistrict(posting.getLocationDistrict())
                .isNewbie(posting.getIsNewbie())
                .thumbnailUrl(posting.getThumbnailUrl())
                .build();
        applyStatus(job, resolveStatus(posting));
        return job;
    }

    public void updateFrom(Job job, JobPosting posting) {
        job.syncFrom(posting.getCompany(), posting.getPosition(), resolveCareerLevel(posting),
                posting.getEmploymentType(), Boolean.TRUE.equals(posting.getIsRemote()),
                posting.getSourceUrl(), posting.getConfirmTime(), posting.getDueTime(),
                resolveStatus(posting),
                posting.getLocationFull(), posting.getMainTasks(), posting.getRequirements(),
                posting.getPreferredPoints(), posting.getSkillTags(), posting.getSkillsInferred(),
                job.getEmbedding(),  // ← null 대신 기존 embedding 유지
                posting.getCategoryChild(), posting.getLocationCity(), posting.getLocationDistrict(),
                posting.getIsNewbie(), posting.getThumbnailUrl());
    }

    private void applyStatus(Job job, JobStatus status) {
        if (status == JobStatus.EXPIRED) {
            job.expire();
        } else if (status == JobStatus.REMOVED) {
            job.markRemoved();
        }
    }

    private JobStatus resolveStatus(JobPosting posting) {
        if (!"active".equalsIgnoreCase(posting.getStatus())) {
            return JobStatus.REMOVED;
        }
        if (posting.getDueTime() != null && posting.getDueTime().isBefore(OffsetDateTime.now())) {
            return JobStatus.EXPIRED;
        }
        return JobStatus.ACTIVE;
    }

    private String resolveCareerLevel(JobPosting posting) {
        if (Boolean.TRUE.equals(posting.getIsNewbie()) && posting.getCareerMin() == null) {
            return "신입";
        }
        if (posting.getCareerMin() != null && posting.getCareerMax() != null) {
            return posting.getCareerMin() + "~" + posting.getCareerMax() + "년";
        }
        if (posting.getCareerMin() != null) {
            return posting.getCareerMin() + "년 이상";
        }
        return "경력무관";
    }

    public JobSummaryResponse toSummaryResponse(Job job) {
        return JobSummaryResponse.builder()
                .id(job.getId())
                .companyName(job.getCompany() != null ? job.getCompany().getName() : null)
                .position(job.getTitle())
                .careerLevel(job.getCareerLevel())
                .employmentType(job.getEmploymentType())
                .remoteAvailable(job.getRemoteAvailable())
                .dueTime(job.getDeadlineAt())
                .thumbnailUrl(job.getThumbnailUrl())
                .skillTags(job.getSkillTags())
                .build();
    }

    public JobDetailResponse toDetailResponse(Job job) {
        return JobDetailResponse.from(job);
    }
}
