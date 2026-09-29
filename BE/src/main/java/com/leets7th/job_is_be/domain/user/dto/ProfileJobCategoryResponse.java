package com.leets7th.job_is_be.domain.user.dto;

public record ProfileJobCategoryResponse(
        Long id,
        String name,
        boolean primary
) {
}
