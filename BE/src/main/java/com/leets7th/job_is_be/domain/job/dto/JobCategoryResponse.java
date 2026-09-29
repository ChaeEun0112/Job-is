package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.JobCategory;

public record JobCategoryResponse(
        Long id,
        String name
) {
    public JobCategoryResponse(JobCategory jobCategory) {
        this(jobCategory.getId(), jobCategory.getName());
    }
}
