package com.leets7th.job_is_be.domain.auth.dto;

import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;

import java.time.OffsetDateTime;

public record SessionResponse(
        Long userId,
        String email,
        SocialType socialType,
        UserStatus status,
        boolean onboardingCompleted,
        OffsetDateTime restorableUntil
) {
}
