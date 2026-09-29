package com.leets7th.job_is_be.domain.content.dto;

import com.leets7th.job_is_be.domain.content.data.ContentItem;
import com.leets7th.job_is_be.domain.content.enums.ContentType;

import java.time.LocalDate;

public record ContentSummaryResponse(
        Long contentId,
        ContentType contentType,
        String tag,
        String title,
        String summary,
        String sourceName,
        LocalDate publishedAt
) {

    public static ContentSummaryResponse from(ContentItem content) {
        return new ContentSummaryResponse(
                content.id(),
                content.contentType(),
                content.contentType().getLabel(),
                content.title(),
                content.summary(),
                content.sourceName(),
                content.publishedAt()
        );
    }
}
