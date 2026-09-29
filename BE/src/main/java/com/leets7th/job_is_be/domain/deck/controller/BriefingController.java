package com.leets7th.job_is_be.domain.deck.controller;

import com.leets7th.job_is_be.domain.deck.dto.BriefingResponse;
import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.service.BriefingService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.domain.deck.controller.docs.BriefingControllerDocs;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/briefings")
@RequiredArgsConstructor
public class BriefingController implements BriefingControllerDocs {

    private final BriefingService briefingService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<BriefingResponse>> getTodayBriefing(
            @AuthenticationPrincipal Jwt jwt
    ) {
        BriefingResponse response = briefingService.getTodayBriefing(Long.valueOf(jwt.getSubject()));
        return ApiResponse.success(SuccessStatus.BRIEFING_TODAY_SUCCESS, response);
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getTodayBriefingStatus(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<CardResponse> response = briefingService.getTodayBriefingStatus(Long.valueOf(jwt.getSubject()));
        return ApiResponse.success(SuccessStatus.BRIEFING_STATUS_SUCCESS, response);
    }
}
