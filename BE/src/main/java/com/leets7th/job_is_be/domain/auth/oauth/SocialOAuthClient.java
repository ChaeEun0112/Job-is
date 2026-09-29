package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.domain.user.enums.SocialType;

import java.net.URI;

public interface SocialOAuthClient {

    SocialType supports();

    URI buildAuthorizationUri(String state);

    OAuthUserInfo getUserInfo(String authorizationCode);
}
