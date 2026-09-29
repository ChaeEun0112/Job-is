package com.leets7th.job_is_be.domain.content.dto;

import com.leets7th.job_is_be.domain.content.data.ContentItem;
import com.leets7th.job_is_be.domain.content.enums.ContentType;

import java.time.LocalDate;

public record ContentDetailResponse(
        Long contentId,
        ContentType contentType,
        String tag,
        String title,
        String summary,
        String body,
        String sourceName,
        LocalDate publishedAt,
        String target,
        LocalDate applicationStartDate,
        LocalDate applicationEndDate,
        String applicationMethod,
        String originalUrl
) {

    public static ContentDetailResponse from(ContentItem content) {
        return new ContentDetailResponse(
                content.id(),
                content.contentType(),
                content.contentType().getLabel(),
                content.title(),
                content.summary(),
                content.body(),
                content.sourceName(),
                content.publishedAt(),
                content.target(),
                content.applicationStartDate(),
                content.applicationEndDate(),
                content.applicationMethod(),
                content.originalUrl()
        );
    }
}
