package com.leets7th.job_is_be.domain.auth.controller.docs;

import com.leets7th.job_is_be.domain.auth.dto.AccountResponse;
import com.leets7th.job_is_be.domain.auth.dto.ConsentRequest;
import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeResponse;
import com.leets7th.job_is_be.domain.auth.dto.RestoreRequest;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalRequest;
import com.leets7th.job_is_be.domain.auth.dto.WithdrawalResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Account", description = "계정 조회/동의/탈퇴/복구 API")
public interface AccountControllerDocs {

    @Operation(
            summary = "계정 정보 조회",
            description = "로그인한 사용자의 계정 정보를 조회합니다. 사용자는 Access Token(JWT)으로 식별됩니다."
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계정 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(summary = "약관 동의 저장", description = "로그인한 사용자의 약관 동의 내역을 저장합니다.")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "약관 동의 저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<Void>> consent(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "약관 동의 저장 요청")
            ConsentRequest request
    );

    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 계정을 탈퇴 처리하고 Refresh Token 쿠키를 제거합니다. 복구 코드가 반환됩니다."
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "탈퇴 성공",
                    headers = @Header(name = "Set-Cookie", description = "Refresh Token 쿠키 삭제")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<WithdrawalResponse>> withdraw(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = false, description = "회원 탈퇴 요청(옵션)")
            WithdrawalRequest request
    );

    @Operation(
            summary = "탈퇴 계정 복구",
            description = "탈퇴 시 발급된 복구 코드로 계정을 복구하고 새 세션을 발급합니다. 로그인 없이 접근 가능합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "복구 성공",
                    headers = @Header(name = "Set-Cookie", description = "Refresh Token 쿠키")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "복구 코드 오류")
    })
    ResponseEntity<ApiResponse<OAuthExchangeResponse>> restore(
            @RequestBody(required = true, description = "탈퇴 계정 복구 요청")
            RestoreRequest request
    );
}
