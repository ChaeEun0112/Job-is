package com.leets7th.job_is_be.domain.auth.dto;

import com.leets7th.job_is_be.domain.user.enums.SocialType;

import java.time.OffsetDateTime;

public record AccountResponse(
        SocialType socialType,
        OffsetDateTime joinedAt,
        String receivingEmail,
        boolean emailVerified
) {
}
