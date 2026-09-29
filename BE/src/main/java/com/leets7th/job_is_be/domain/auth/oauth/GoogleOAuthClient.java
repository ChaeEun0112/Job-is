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
public class GoogleOAuthClient implements SocialOAuthClient {

    private final OAuthProperties.Provider properties;
    private final RestClient restClient;

    public GoogleOAuthClient(
            OAuthProperties oauthProperties,
            @Qualifier("oauthRestClient") RestClient restClient
    ) {
        this.properties = oauthProperties.google();
        this.restClient = restClient;
    }

    @Override
    public SocialType supports() {
        return SocialType.GOOGLE;
    }

    @Override
    public URI buildAuthorizationUri(String state) {
        validateConfigured();
        return UriComponentsBuilder.fromUri(properties.authorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("scope", String.join(" ", properties.scopes()))
                .queryParam("state", state)
                .build()
                .encode()
                .toUri();
    }

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        validateConfigured();
        try {
            GoogleTokenResponse token = requestToken(authorizationCode);
            GoogleUserResponse user = restClient.get()
                    .uri(properties.userInfoUri())
                    .headers(headers -> headers.setBearerAuth(token.accessToken()))
                    .retrieve()
                    .body(GoogleUserResponse.class);

            if (user == null || user.sub() == null || user.sub().isBlank()) {
                throw new GeneralException(ErrorStatus.OAUTH_PROVIDER_ERROR);
            }
            if (user.email() == null || user.email().isBlank() || !Boolean.TRUE.equals(user.emailVerified())) {
                throw new GeneralException(ErrorStatus.OAUTH_EMAIL_REQUIRED);
            }
            return new OAuthUserInfo(user.sub(), SocialType.GOOGLE, user.email());
        } catch (GeneralException e) {
            throw e;
        } catch (RestClientException e) {
            throw new GeneralException(ErrorStatus.OAUTH_PROVIDER_ERROR);
        }
    }

    private GoogleTokenResponse requestToken(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", properties.redirectUri().toString());
        form.add("code", authorizationCode);

        GoogleTokenResponse token = restClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);
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

    private record GoogleTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {
    }

    private record GoogleUserResponse(
            String sub,
            String email,
            @JsonProperty("email_verified") Boolean emailVerified
    ) {
    }
}
