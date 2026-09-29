package com.leets7th.job_is_be.domain.notification.controller;

import com.leets7th.job_is_be.domain.notification.controller.docs.NotificationSettingControllerDocs;
import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingResponse;
import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingUpdateRequest;
import com.leets7th.job_is_be.domain.notification.dto.SnoozeRequest;
import com.leets7th.job_is_be.domain.notification.service.NotificationSettingService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings/notification")
@RequiredArgsConstructor
public class NotificationSettingController implements NotificationSettingControllerDocs {

    private final NotificationSettingService notificationSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getSetting(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                SuccessStatus.NOTIFICATION_SETTING_GET_SUCCESS,
                notificationSettingService.getSetting(userId(jwt))
        );
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSetting(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody NotificationSettingUpdateRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.NOTIFICATION_SETTING_UPDATE_SUCCESS,
                notificationSettingService.updateSetting(userId(jwt), request)
        );
    }

    @PostMapping("/snooze")
    public ResponseEntity<ApiResponse<Void>> snooze(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SnoozeRequest request
    ) {
        notificationSettingService.snooze(userId(jwt), request);
        return ApiResponse.success(SuccessStatus.NOTIFICATION_SNOOZE_SUCCESS);
    }

    @DeleteMapping("/snooze")
    public ResponseEntity<ApiResponse<Void>> cancelSnooze(
            @AuthenticationPrincipal Jwt jwt
    ) {
        notificationSettingService.cancelSnooze(userId(jwt));
        return ApiResponse.success(SuccessStatus.NOTIFICATION_SNOOZE_CANCEL_SUCCESS);
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
