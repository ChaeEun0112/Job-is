package com.leets7th.job_is_be.domain.auth.controller.docs;

import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeRequest;
import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

@Tag(name = "OAuth", description = "소셜 로그인(OAuth) 인가/콜백/코드 교환 API")
public interface OAuthControllerDocs {

    @Operation(
            summary = "OAuth 인가 요청",
            description = "지정한 provider(kakao/google)의 인가 페이지로 리다이렉트하고 state 쿠키를 설정합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "302",
                    description = "인가 페이지로 리다이렉트",
                    headers = {
                            @Header(name = "Location", description = "OAuth provider 인가 URL"),
                            @Header(name = "Set-Cookie", description = "OAuth state 쿠키")
                    }
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 provider")
    })
    ResponseEntity<Void> authorize(
            @Parameter(description = "OAuth provider (kakao/google)") String provider
    );

    @Operation(
            summary = "OAuth 콜백",
            description = "OAuth provider의 콜백을 처리하고 프론트엔드 성공/실패 URL로 리다이렉트합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "302",
                    description = "프론트엔드 성공/실패 페이지로 리다이렉트",
                    headers = {
                            @Header(name = "Location", description = "프론트엔드 리다이렉트 URL"),
                            @Header(name = "Set-Cookie", description = "OAuth state 쿠키 삭제")
                    }
            )
    })
    ResponseEntity<Void> callback(
            @Parameter(description = "OAuth provider (kakao/google)") String provider,
            @Parameter(description = "인가 코드") String code,
            @Parameter(description = "CSRF 방지용 state 값") String state,
            @Parameter(description = "provider 에러 코드") String error,
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(
            summary = "로그인 코드 교환",
            description = "콜백에서 전달받은 로그인 코드를 Access Token/Refresh Token 세션으로 교환합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "교환 성공",
                    headers = @Header(name = "Set-Cookie", description = "Refresh Token 쿠키")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "로그인 코드 오류")
    })
    ResponseEntity<ApiResponse<OAuthExchangeResponse>> exchange(
            @RequestBody(required = true, description = "로그인 코드 교환 요청")
            OAuthExchangeRequest request
    );
}
