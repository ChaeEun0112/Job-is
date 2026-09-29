package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.Region;

public record RegionResponse(
        Long id,
        String name
) {
    public RegionResponse(Region region) {
        this(region.getId(), region.getName());
    }
}
