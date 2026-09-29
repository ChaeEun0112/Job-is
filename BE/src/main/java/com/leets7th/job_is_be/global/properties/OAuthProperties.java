package com.leets7th.job_is_be.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app.oauth")
public record OAuthProperties(
        URI frontendSuccessUri,
        URI frontendFailureUri,
        Duration stateTtl,
        Duration loginCodeTtl,
        Duration connectTimeout,
        Duration readTimeout,
        Provider kakao,
        Provider google
) {

    public OAuthProperties {
        if (stateTtl == null || stateTtl.isNegative() || stateTtl.isZero()) {
            throw new IllegalArgumentException("OAuth state TTL must be positive");
        }
        if (loginCodeTtl == null || loginCodeTtl.isNegative() || loginCodeTtl.isZero()) {
            throw new IllegalArgumentException("OAuth login code TTL must be positive");
        }
        if (connectTimeout == null || connectTimeout.isNegative() || connectTimeout.isZero()) {
            throw new IllegalArgumentException("OAuth connect timeout must be positive");
        }
        if (readTimeout == null || readTimeout.isNegative() || readTimeout.isZero()) {
            throw new IllegalArgumentException("OAuth read timeout must be positive");
        }
    }

    public record Provider(
            String clientId,
            String clientSecret,
            URI redirectUri,
            URI authorizationUri,
            URI tokenUri,
            URI userInfoUri,
            List<String> scopes
    ) {

        public boolean isConfigured() {
            return clientId != null && !clientId.isBlank()
                    && clientSecret != null && !clientSecret.isBlank();
        }
    }
}
