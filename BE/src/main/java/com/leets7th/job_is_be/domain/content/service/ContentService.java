package com.leets7th.job_is_be.domain.content.service;

import com.leets7th.job_is_be.domain.content.data.ContentData;
import com.leets7th.job_is_be.domain.content.data.ContentItem;
import com.leets7th.job_is_be.domain.content.dto.ContentDetailResponse;
import com.leets7th.job_is_be.domain.content.dto.ContentSummaryResponse;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ContentService {

    public List<ContentSummaryResponse> getContents() {
        return ContentData.getContents().stream()
                .sorted(Comparator.comparingInt(ContentItem::displayOrder))
                .map(ContentSummaryResponse::from)
                .toList();
    }

    public ContentDetailResponse getContent(Long contentId) {
        if (contentId == null) {
            throw new GeneralException(ErrorStatus.CONTENT_NOT_FOUND);
        }

        return ContentData.getContents().stream()
                .filter(content -> content.id().equals(contentId))
                .findFirst()
                .map(ContentDetailResponse::from)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CONTENT_NOT_FOUND));
    }
}
