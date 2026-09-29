package com.leets7th.job_is_be.domain.auth.controller;

import com.leets7th.job_is_be.domain.auth.controller.docs.DevAuthControllerDocs;
import com.leets7th.job_is_be.domain.auth.dto.DevLoginRequest;
import com.leets7th.job_is_be.domain.auth.dto.DevLoginResponse;
import com.leets7th.job_is_be.domain.auth.service.DevAuthService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/auth/dev")
public class DevAuthController implements DevAuthControllerDocs {

    private final DevAuthService devAuthService;

    public DevAuthController(DevAuthService devAuthService) {
        this.devAuthService = devAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<DevLoginResponse>> login(
            @RequestBody DevLoginRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.DEV_LOGIN_SUCCESS,
                devAuthService.login(request.userId())
        );
    }
}
