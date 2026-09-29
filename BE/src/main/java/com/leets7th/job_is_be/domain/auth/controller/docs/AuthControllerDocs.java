package com.leets7th.job_is_be.domain.auth.controller.docs;

import com.leets7th.job_is_be.domain.auth.dto.CsrfTokenResponse;
import com.leets7th.job_is_be.domain.auth.dto.SessionResponse;
import com.leets7th.job_is_be.domain.auth.dto.TokenReissueResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Auth", description = "CSRF 토큰, 토큰 재발급, 세션 조회, 로그아웃 API")
public interface AuthControllerDocs {

    @Operation(
            summary = "CSRF 토큰 발급",
            description = "Refresh Token 기반 요청(토큰 재발급/로그아웃)에 필요한 CSRF 토큰을 발급합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CSRF 토큰 발급 성공",
                    headers = @Header(name = "Set-Cookie", description = "XSRF-TOKEN 쿠키")
            )
    })
    ResponseEntity<ApiResponse<CsrfTokenResponse>> csrf(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "Access Token 재발급",
            description = "쿠키의 Refresh Token으로 새 Access Token을 재발급합니다. CSRF 토큰 검증이 필요합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    headers = @Header(name = "Set-Cookie", description = "회전된 Refresh Token 쿠키")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CSRF 토큰 검증 실패")
    })
    ResponseEntity<ApiResponse<TokenReissueResponse>> reissue(
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(
            summary = "현재 세션 조회",
            description = "Access Token(JWT)에 해당하는 현재 로그인 사용자의 세션 정보를 조회합니다."
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "세션 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<SessionResponse>> me(
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화하고 관련 쿠키를 제거합니다. CSRF 토큰 검증이 필요합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    headers = @Header(name = "Set-Cookie", description = "Refresh Token 쿠키 삭제")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CSRF 토큰 검증 실패")
    })
    ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true) HttpServletRequest request
    );
}
