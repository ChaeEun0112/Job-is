package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.user.enums.CareerLevel;

public record CareerLevelResponse(
        String key,
        String description,
        Integer minYears,
        Integer maxYears
) {
    public CareerLevelResponse(CareerLevel level) {
        this(
                level != null ? level.name() : null,
                level != null ? level.getDescription() : null,
                level != null ? switch (level) {
                    case ENTRY -> 0;
                    case JUNIOR -> 1;
                    case EXPERIENCED -> 4;
                } : null,
                level != null ? switch (level) {
                    case ENTRY -> 0;
                    case JUNIOR -> 3;
                    case EXPERIENCED -> 99;
                } : null
        );
    }
}
