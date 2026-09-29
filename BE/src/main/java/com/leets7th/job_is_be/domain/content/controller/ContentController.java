package com.leets7th.job_is_be.domain.content.controller;

import com.leets7th.job_is_be.domain.content.controller.docs.ContentControllerDocs;
import com.leets7th.job_is_be.domain.content.dto.ContentDetailResponse;
import com.leets7th.job_is_be.domain.content.dto.ContentSummaryResponse;
import com.leets7th.job_is_be.domain.content.service.ContentService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
public class ContentController implements ContentControllerDocs {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentSummaryResponse>>> getContents() {
        return ApiResponse.success(
                SuccessStatus.CONTENT_LIST_GET_SUCCESS,
                contentService.getContents()
        );
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getContent(
            @PathVariable Long contentId
    ) {
        return ApiResponse.success(
                SuccessStatus.CONTENT_DETAIL_GET_SUCCESS,
                contentService.getContent(contentId)
        );
    }
}
