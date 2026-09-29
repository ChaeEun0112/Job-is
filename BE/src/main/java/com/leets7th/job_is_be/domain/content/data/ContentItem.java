package com.leets7th.job_is_be.domain.content.data;

import com.leets7th.job_is_be.domain.content.enums.ContentType;

import java.time.LocalDate;

public record ContentItem(
        Long id,
        ContentType contentType,
        String title,
        String summary,
        String body,
        String sourceName,
        LocalDate publishedAt,
        String target,
        LocalDate applicationStartDate,
        LocalDate applicationEndDate,
        String applicationMethod,
        String originalUrl,
        int displayOrder
) {
}
