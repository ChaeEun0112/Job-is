package com.leets7th.job_is_be.domain.notification.controller.docs;

import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingResponse;
import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingUpdateRequest;
import com.leets7th.job_is_be.domain.notification.dto.SnoozeRequest;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "NotificationSetting", description = "알림 설정 및 스누즈 API")
@SecurityRequirement(name = "JWT")
public interface NotificationSettingControllerDocs {

    @Operation(
            summary = "알림 설정 조회",
            description = "로그인한 사용자의 알림 설정을 조회합니다. 사용자는 Access Token(JWT)으로 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<NotificationSettingResponse>> getSetting(
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(summary = "알림 설정 수정", description = "로그인한 사용자의 알림 설정을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSetting(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "알림 설정 수정 요청")
            NotificationSettingUpdateRequest request
    );

    @Operation(summary = "알림 스누즈 설정", description = "지정한 기간 동안 알림을 일시 중지(스누즈)합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<Void>> snooze(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "알림 스누즈 요청")
            SnoozeRequest request
    );

    @Operation(summary = "알림 스누즈 해제", description = "설정된 알림 스누즈를 즉시 해제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<Void>> cancelSnooze(
            @Parameter(hidden = true) Jwt jwt
    );
}
