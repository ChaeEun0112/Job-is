package com.leets7th.job_is_be.domain.user.dto;

public record ResumeDownloadUrlResponse(
        String downloadUrl,
        String fileName,
        long expiresIn
) {
}
