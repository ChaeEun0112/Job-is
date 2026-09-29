package com.leets7th.job_is_be.domain.user.controller;

import com.leets7th.job_is_be.domain.user.controller.docs.ProfileControllerDocs;
import com.leets7th.job_is_be.domain.user.dto.ProfileDraftRequest;
import com.leets7th.job_is_be.domain.user.dto.ProfileDraftResponse;
import com.leets7th.job_is_be.domain.user.dto.ProfileResponse;
import com.leets7th.job_is_be.domain.user.dto.ProfileUpdateRequest;
import com.leets7th.job_is_be.domain.user.service.ProfileService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController implements ProfileControllerDocs {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                SuccessStatus.PROFILE_GET_SUCCESS,
                profileService.getProfile(userId(jwt))
        );
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ProfileUpdateRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.PROFILE_UPDATE_SUCCESS,
                profileService.updateProfile(userId(jwt), request)
        );
    }

    @GetMapping("/draft")
    public ResponseEntity<ApiResponse<ProfileDraftResponse>> getDraft(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                SuccessStatus.PROFILE_DRAFT_GET_SUCCESS,
                profileService.getDraft(userId(jwt))
        );
    }

    @PutMapping("/draft")
    public ResponseEntity<ApiResponse<ProfileDraftResponse>> saveDraft(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ProfileDraftRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.PROFILE_DRAFT_SAVE_SUCCESS,
                profileService.saveDraft(userId(jwt), request)
        );
    }

    @PostMapping("/onboarding/complete")
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @AuthenticationPrincipal Jwt jwt
    ) {
        profileService.completeOnboarding(userId(jwt));
        return ApiResponse.success(SuccessStatus.PROFILE_ONBOARDING_COMPLETE_SUCCESS);
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
