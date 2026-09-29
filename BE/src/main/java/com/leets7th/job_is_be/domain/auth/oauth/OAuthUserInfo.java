package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.domain.user.enums.SocialType;

public record OAuthUserInfo(
        String socialId,
        SocialType socialType,
        String email
) {
}
