package com.leets7th.job_is_be.domain.auth.dto;

public record DevLoginResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
