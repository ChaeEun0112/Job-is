package com.leets7th.job_is_be.global.config;

import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.properties.JwtProperties;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    /**
     * Refresh Token이 쿠키(HttpOnly)로 전달되는 엔드포인트만
     * CSRF 보호를 적용한다.
     *
     * Access Token 인증 API는 Authorization 헤더를 사용하므로
     * CSRF 공격 대상이 아니다.
     */
    private static final RequestMatcher REFRESH_COOKIE_CSRF_MATCHER = request -> {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());

        return "/api/auth/token/reissue".equals(path)
                || "/api/auth/logout".equals(path);
    };

    /**
     * CSRF 토큰을 JavaScript에서 읽을 수 있는 Cookie에 저장한다.
     * 프론트는 이 값을 X-CSRF-TOKEN 헤더로 전송한다.
     */
    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository(JwtProperties jwtProperties) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .secure(jwtProperties.cookieSecure())
                .sameSite(jwtProperties.cookieSameSite()));

        return repository;
    }

    /**
     * Access Token의 "role" claim을 ROLE_* 권한으로 변환한다.
     * (기본 컨버터는 scope/scp claim만 읽으므로 커스텀 변환이 필요하다.)
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            if (role == null || role.isBlank()) {
                return List.of();
            }
            return List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            @Qualifier("accessTokenDecoder") JwtDecoder accessTokenDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CookieCsrfTokenRepository csrfTokenRepository,
            ObjectMapper objectMapper
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                // Refresh Token을 사용하는 API만 CSRF 검증
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(REFRESH_COOKIE_CSRF_MATCHER))

                // JWT 기반 인증이므로 Session을 생성하지 않는다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/health",
                                "/api/auth/csrf",
                                "/api/auth/restore",
                                "/api/auth/dev/**",
                                "/api/auth/oauth/**",
                                "/api/unsubscribe/**"
                        ).permitAll()

                        // Refresh Token API는 Access Token 없이 접근 가능
                        // 대신 CSRF 검증을 반드시 통과해야 한다.
                        .requestMatchers(REFRESH_COOKIE_CSRF_MATCHER).permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 나머지 API는 Access Token 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT(Resource Server) 인증 설정
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(accessTokenDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))

                        // 인증 실패(401)
                        .authenticationEntryPoint((request, response, exception) ->
                                writeErrorResponse(
                                        response,
                                        objectMapper,
                                        ErrorStatus.UNAUTHORIZED))

                        // 권한 부족(403)
                        .accessDeniedHandler((request, response, exception) ->
                                writeErrorResponse(
                                        response,
                                        objectMapper,
                                        ErrorStatus.FORBIDDEN))
                );

        return http.build();
    }

    /**
     * Spring Security 기본 HTML 응답 대신
     * 프로젝트 공통 JSON 응답 형식으로 반환한다.
     */
    private void writeErrorResponse(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            ErrorStatus errorStatus
    ) throws IOException {

        response.setStatus(errorStatus.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                new ApiResponse<>(
                        false,
                        errorStatus.getCode(),
                        errorStatus.getMessage(),
                        null
                )
        );
    }
}
