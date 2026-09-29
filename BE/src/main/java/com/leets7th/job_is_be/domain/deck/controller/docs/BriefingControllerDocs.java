package com.leets7th.job_is_be.domain.deck.controller.docs;

import com.leets7th.job_is_be.domain.deck.dto.BriefingResponse;
import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Tag(name = "Briefing", description = "오늘의 브리핑(추천 덱 요약) 조회 API")
@SecurityRequirement(name = "JWT")
public interface BriefingControllerDocs {

    @Operation(
            summary = "오늘의 브리핑 조회",
            description = "로그인한 사용자의 오늘 추천 덱 메타(덱 ID·일자·회차·인사말·건수·테마)를 조회합니다. "
                    + "덱이 없거나 카드가 0건이어도 오류가 아니라 state 코드"
                    + "(pre_slot / no_candidates / onboarding_incomplete)가 담긴 정상 응답을 돌려줍니다. "
                    + "firstOpenedAt 이 null 이면 당일 첫 방문(인트로 노출), 값이 있으면 재방문(인트로 생략)입니다. "
                    + "이 API 호출 시점에 최초 열람 시각이 기록됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<BriefingResponse>> getTodayBriefing(
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "오늘의 브리핑 카드 상태 조회",
            description = "로그인한 사용자의 오늘 브리핑에 포함된 카드들의 처리 상태(열람/관심없음 등)를 조회합니다. "
                    + "오늘 덱이 없으면 빈 배열을 반환하며, 빈 상태의 원인은 브리핑 조회 API의 state 로 판정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<CardResponse>>> getTodayBriefingStatus(
            @Parameter(hidden = true) Jwt jwt
    );
}
