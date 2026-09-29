package com.leets7th.job_is_be.domain.user.controller.docs;

import com.leets7th.job_is_be.domain.user.dto.ProfileDraftRequest;
import com.leets7th.job_is_be.domain.user.dto.ProfileDraftResponse;
import com.leets7th.job_is_be.domain.user.dto.ProfileResponse;
import com.leets7th.job_is_be.domain.user.dto.ProfileUpdateRequest;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Profile", description = "사용자 프로필 및 온보딩 API")
@SecurityRequirement(name = "JWT")
public interface ProfileControllerDocs {

    @Operation(
            summary = "프로필 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다. 사용자는 Access Token(JWT)으로 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(summary = "프로필 수정", description = "로그인한 사용자의 프로필 정보를 부분 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "프로필 수정 요청")
            ProfileUpdateRequest request
    );

    @Operation(summary = "프로필 임시저장 조회", description = "온보딩 진행 중 임시저장된 프로필 초안을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<ProfileDraftResponse>> getDraft(
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(summary = "프로필 임시저장", description = "온보딩 진행 중 프로필 초안을 임시저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "임시저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<ProfileDraftResponse>> saveDraft(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "프로필 임시저장 요청")
            ProfileDraftRequest request
    );

    @Operation(summary = "온보딩 완료", description = "온보딩 절차를 완료 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "완료 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @Parameter(hidden = true) Jwt jwt
    );
}
