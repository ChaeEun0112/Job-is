package com.leets7th.job_is_be.domain.user.dto;

public record PresignedUrlResponse(
        String presignedUrl,
        String objectKey,
        long expiresIn
) {
}
