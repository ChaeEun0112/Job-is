package com.leets7th.job_is_be.domain.auth.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.properties.OAuthProperties;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class KakaoOAuthClient implements SocialOAuthClient {

    private final OAuthProperties.Provider properties;
    private final RestClient restClient;

    public KakaoOAuthClient(
            OAuthProperties oauthProperties,
            @Qualifier("oauthRestClient") RestClient restClient
    ) {
        this.properties = oauthProperties.kakao();
        this.restClient = restClient;
    }

    @Override
    public SocialType supports() {
        return SocialType.KAKAO;
    }

    @Override
    public URI buildAuthorizationUri(String state) {
        validateConfigured();
        return UriComponentsBuilder.fromUri(properties.authorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("scope", String.join(",", properties.scopes()))
                .queryParam("state", state)
                .build()
                .encode()
                .toUri();
    }

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        validateConfigured();
        try {
            KakaoTokenResponse token = requestToken(authorizationCode);
            KakaoUserResponse user = restClient.get()
                    .uri(properties.userInfoUri())
                    .headers(headers -> headers.setBearerAuth(token.accessToken()))
                    .retrieve()
                    .body(KakaoUserResponse.class);

            if (user == null || user.id() == null) {
                throw new GeneralException(ErrorStatus.OAUTH_PROVIDER_ERROR);
            }
            KakaoAccount account = user.kakaoAccount();
            String email = account == null ? null : account.email();
            validateEmail(email, account == null ? null : account.emailVerified());
            return new OAuthUserInfo(user.id().toString(), SocialType.KAKAO, email);
        } catch (GeneralException e) {
            throw e;
        } catch (RestClientException e) {
            throw new GeneralException(ErrorStatus.OAUTH_PROVIDER_ERROR);
        }
    }

    private KakaoTokenResponse requestToken(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", properties.redirectUri().toString());
        form.add("code", authorizationCode);

        KakaoTokenResponse token = restClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class);
        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new GeneralException(ErrorStatus.OAUTH_PROVIDER_ERROR);
        }
        return token;
    }

    private void validateConfigured() {
        if (!properties.isConfigured()) {
            throw new GeneralException(ErrorStatus.OAUTH_NOT_CONFIGURED);
        }
    }

    private void validateEmail(String email, Boolean emailVerified) {
        if (email == null || email.isBlank() || !Boolean.TRUE.equals(emailVerified)) {
            throw new GeneralException(ErrorStatus.OAUTH_EMAIL_REQUIRED);
        }
    }

    private record KakaoTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {
    }

    private record KakaoUserResponse(
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
    }

    private record KakaoAccount(
            String email,
            @JsonProperty("is_email_verified") Boolean emailVerified
    ) {
    }
}
