package com.leets7th.job_is_be.domain.auth.controller.docs;

import com.leets7th.job_is_be.domain.auth.dto.DevLoginRequest;
import com.leets7th.job_is_be.domain.auth.dto.DevLoginResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "DevAuth", description = "개발용 로그인 API (dev 프로필 전용)")
public interface DevAuthControllerDocs {

    @Operation(
            summary = "개발용 로그인",
            description = "userId로 즉시 로그인하여 토큰을 발급받는 개발 전용 엔드포인트입니다. dev 프로필에서만 활성화됩니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류")
    })
    ResponseEntity<ApiResponse<DevLoginResponse>> login(
            @RequestBody(required = true, description = "개발 로그인 요청(userId)")
            DevLoginRequest request
    );
}
