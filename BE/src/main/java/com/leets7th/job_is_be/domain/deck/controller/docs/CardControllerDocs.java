package com.leets7th.job_is_be.domain.deck.controller.docs;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.dto.DismissReasonRequest;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Card", description = "추천 덱 카드 처리(관심없음/관심없음 취소/사유 제출) API")
@SecurityRequirement(name = "JWT")
public interface CardControllerDocs {

    @Operation(
            summary = "카드 관심없음",
            description = "덱에 포함된 특정 카드를 관심없음 처리합니다. 본인의 덱/카드만 처리할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "관심없음 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 덱/카드가 아님")
    })
    ResponseEntity<ApiResponse<CardResponse>> dismissCard(
            @Parameter(description = "덱 ID") Long deckId,
            @Parameter(description = "카드 ID") Long cardId,
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "카드 관심없음 취소",
            description = "관심없음 처리했던 카드를 다시 활성 상태로 되돌립니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "관심없음 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 덱/카드가 아님")
    })
    ResponseEntity<ApiResponse<CardResponse>> cancelDismissCard(
            @Parameter(description = "덱 ID") Long deckId,
            @Parameter(description = "카드 ID") Long cardId,
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(
            summary = "카드 관심없음 사유 제출",
            description = "관심없음 처리한 카드에 대한 사유를 제출합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사유 제출 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 덱/카드가 아님")
    })
    ResponseEntity<ApiResponse<Void>> submitDismissReason(
            @Parameter(description = "덱 ID") Long deckId,
            @Parameter(description = "카드 ID") Long cardId,
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "카드 관심없음 사유 제출 요청")
            DismissReasonRequest request
    );
}
