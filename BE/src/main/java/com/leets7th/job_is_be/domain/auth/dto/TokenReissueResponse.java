package com.leets7th.job_is_be.domain.auth.dto;

public record TokenReissueResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
