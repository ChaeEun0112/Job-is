package com.leets7th.job_is_be.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final JwtAuthenticationConverter converter = new SecurityConfig().jwtAuthenticationConverter();

    @Test
    void mapsRoleClaimToRoleAuthority() {
        Jwt jwt = jwt(Map.of("role", "ADMIN"));

        var authorities = converter.convert(jwt).getAuthorities();

        assertThat(authorities).extracting("authority").contains("ROLE_ADMIN");
    }

    @Test
    void mapsUserRoleClaimToUserAuthority() {
        Jwt jwt = jwt(Map.of("role", "USER"));

        var authorities = converter.convert(jwt).getAuthorities();

        assertThat(authorities).extracting("authority").contains("ROLE_USER");
    }

    @Test
    void grantsNoRoleAuthorityWhenRoleClaimMissing() {
        Jwt jwt = jwt(Map.of());

        var authorities = converter.convert(jwt).getAuthorities();

        assertThat(authorities).extracting("authority")
                .noneMatch(authority -> authority.toString().startsWith("ROLE_"));
    }

    private Jwt jwt(Map<String, Object> extraClaims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claims(claims -> claims.putAll(extraClaims))
                .build();
    }
}
