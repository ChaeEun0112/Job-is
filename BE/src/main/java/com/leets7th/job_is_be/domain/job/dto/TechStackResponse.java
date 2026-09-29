package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.enums.TechStackType;

public record TechStackResponse(
        String code,
        String name
) {
    public static TechStackResponse from(TechStackType type) {
        return new TechStackResponse(type.name(), type.getValue());
    }
}
