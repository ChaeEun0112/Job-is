package com.leets7th.job_is_be.domain.user.controller;

import com.leets7th.job_is_be.domain.user.controller.docs.ResumeControllerDocs;
import com.leets7th.job_is_be.domain.user.dto.PresignedUrlRequest;
import com.leets7th.job_is_be.domain.user.dto.PresignedUrlResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeConfirmRequest;
import com.leets7th.job_is_be.domain.user.dto.ResumeDownloadUrlResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeUploadResponse;
import com.leets7th.job_is_be.domain.user.service.ResumeService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile/files")
public class ResumeController implements ResumeControllerDocs {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> issuePresignedUrl(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = resumeService.issuePresignedUrl(userId(jwt), request);
        return ApiResponse.success(SuccessStatus.RESUME_PRESIGNED_URL_SUCCESS, response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResumeUploadResponse>> confirmUpload(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ResumeConfirmRequest request
    ) {
        ResumeUploadResponse response = resumeService.confirmUpload(userId(jwt), request);
        SuccessStatus status = response.created()
                ? SuccessStatus.RESUME_UPLOAD_CONFIRM_SUCCESS
                : SuccessStatus.RESUME_UPLOAD_UPDATE_SUCCESS;
        return ApiResponse.success(status, response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getFiles(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<ResumeResponse> response = resumeService.getFiles(userId(jwt));
        return ApiResponse.success(SuccessStatus.RESUME_LIST_SUCCESS, response);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long fileId
    ) {
        resumeService.deleteFile(userId(jwt), fileId);
        return ApiResponse.success(SuccessStatus.RESUME_DELETE_SUCCESS);
    }

    @GetMapping("/{fileId}/download-url")
    public ResponseEntity<ApiResponse<ResumeDownloadUrlResponse>> issueDownloadUrl(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long fileId
    ) {
        ResumeDownloadUrlResponse response = resumeService.issueDownloadUrl(userId(jwt), fileId);
        return ApiResponse.success(SuccessStatus.RESUME_DOWNLOAD_URL_SUCCESS, response);
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
