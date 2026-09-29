package com.leets7th.job_is_be.domain.notification.controller.docs;

import com.leets7th.job_is_be.domain.notification.dto.UnsubscribeFeedbackRequest;
import com.leets7th.job_is_be.domain.notification.dto.UnsubscribeResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Unsubscribe", description = "토큰 기반 이메일 수신거부 API (로그인 불필요)")
@SecurityRequirements
public interface UnsubscribeControllerDocs {

    @Operation(
            summary = "수신거부",
            description = "이메일에 포함된 토큰으로 알림 수신을 거부합니다. 로그인 없이 접근 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수신거부 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    ResponseEntity<ApiResponse<UnsubscribeResponse>> unsubscribe(
            @Parameter(description = "수신거부 토큰") String token
    );

    @Operation(summary = "재구독", description = "수신거부했던 사용자가 토큰으로 알림 수신을 다시 활성화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재구독 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    ResponseEntity<ApiResponse<UnsubscribeResponse>> resubscribe(
            @Parameter(description = "수신거부 토큰") String token
    );

    @Operation(summary = "수신거부 사유 제출", description = "수신거부에 대한 사유 피드백을 제출합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "피드백 제출 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 또는 토큰 오류")
    })
    ResponseEntity<ApiResponse<Void>> submitFeedback(
            @Parameter(description = "수신거부 토큰") String token,
            @RequestBody(required = true, description = "수신거부 사유 제출 요청")
            UnsubscribeFeedbackRequest request
    );
}
