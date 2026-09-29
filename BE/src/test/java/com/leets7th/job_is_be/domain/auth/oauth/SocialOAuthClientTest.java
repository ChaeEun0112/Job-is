package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.properties.OAuthProperties;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SocialOAuthClientTest {

    @Test
    void exchangesKakaoCodeAndMapsUserInfo() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoOAuthClient client = new KakaoOAuthClient(properties(), builder.build());
        server.expect(once(), requestTo("https://provider.example/kakao/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"kakao-access\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("https://provider.example/kakao/user"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer kakao-access"))
                .andRespond(withSuccess(
                        "{\"id\":12345,\"kakao_account\":{\"email\":\"user@example.com\",\"is_email_verified\":true}}",
                        MediaType.APPLICATION_JSON
                ));

        OAuthUserInfo userInfo = client.getUserInfo("authorization-code");

        assertEquals("12345", userInfo.socialId());
        assertEquals(SocialType.KAKAO, userInfo.socialType());
        assertEquals("user@example.com", userInfo.email());
        server.verify();
    }

    @Test
    void rejectsKakaoAccountWithUnverifiedEmail() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoOAuthClient client = new KakaoOAuthClient(properties(), builder.build());
        server.expect(once(), requestTo("https://provider.example/kakao/token"))
                .andRespond(withSuccess("{\"access_token\":\"kakao-access\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("https://provider.example/kakao/user"))
                .andRespond(withSuccess(
                        "{\"id\":12345,\"kakao_account\":{\"email\":\"user@example.com\",\"is_email_verified\":false}}",
                        MediaType.APPLICATION_JSON
                ));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> client.getUserInfo("authorization-code")
        );

        assertEquals(ErrorStatus.OAUTH_EMAIL_REQUIRED, exception.getErrorStatus());
        server.verify();
    }

    @Test
    void exchangesGoogleCodeAndMapsVerifiedEmail() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GoogleOAuthClient client = new GoogleOAuthClient(properties(), builder.build());
        server.expect(once(), requestTo("https://provider.example/google/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"google-access\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("https://provider.example/google/user"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer google-access"))
                .andRespond(withSuccess(
                        "{\"sub\":\"google-id\",\"email\":\"user@example.com\",\"email_verified\":true}",
                        MediaType.APPLICATION_JSON
                ));

        OAuthUserInfo userInfo = client.getUserInfo("authorization-code");

        assertEquals("google-id", userInfo.socialId());
        assertEquals(SocialType.GOOGLE, userInfo.socialType());
        assertEquals("user@example.com", userInfo.email());
        server.verify();
    }

    @Test
    void authorizationUrisContainStateAndRegisteredRedirectUri() {
        KakaoOAuthClient kakao = new KakaoOAuthClient(properties(), RestClient.create());
        GoogleOAuthClient google = new GoogleOAuthClient(properties(), RestClient.create());

        String kakaoUri = kakao.buildAuthorizationUri("state-value").toString();
        String googleUri = google.buildAuthorizationUri("state-value").toString();

        assertTrue(kakaoUri.contains("state=state-value"));
        assertTrue(kakaoUri.contains("redirect_uri=http://localhost:8081/api/auth/oauth/kakao/callback"));
        assertTrue(googleUri.contains("state=state-value"));
        assertTrue(googleUri.contains("redirect_uri=http://localhost:8081/api/auth/oauth/google/callback"));
    }

    private OAuthProperties properties() {
        OAuthProperties.Provider kakao = new OAuthProperties.Provider(
                "kakao-client", "kakao-secret",
                URI.create("http://localhost:8081/api/auth/oauth/kakao/callback"),
                URI.create("https://provider.example/kakao/authorize"),
                URI.create("https://provider.example/kakao/token"),
                URI.create("https://provider.example/kakao/user"),
                List.of("account_email")
        );
        OAuthProperties.Provider google = new OAuthProperties.Provider(
                "google-client", "google-secret",
                URI.create("http://localhost:8081/api/auth/oauth/google/callback"),
                URI.create("https://provider.example/google/authorize"),
                URI.create("https://provider.example/google/token"),
                URI.create("https://provider.example/google/user"),
                List.of("openid", "email")
        );
        return new OAuthProperties(
                URI.create("http://localhost:5173/oauth/callback"),
                URI.create("http://localhost:5173/oauth/callback"),
                Duration.ofMinutes(5),
                Duration.ofMinutes(1),
                Duration.ofSeconds(3),
                Duration.ofSeconds(5),
                kakao,
                google
        );
    }
}
