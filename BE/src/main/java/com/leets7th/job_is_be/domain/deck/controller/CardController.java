package com.leets7th.job_is_be.domain.deck.controller;

import com.leets7th.job_is_be.domain.deck.controller.docs.CardControllerDocs;
import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.dto.DismissReasonRequest;
import com.leets7th.job_is_be.domain.deck.service.CardService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class CardController implements CardControllerDocs {

    private final CardService cardService;

    @PostMapping("/{deckId}/cards/{cardId}/dismiss")
    public ResponseEntity<ApiResponse<CardResponse>> dismissCard(
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CardResponse response = cardService.dismissCard(deckId, cardId, Long.valueOf(jwt.getSubject()));
        return ApiResponse.success(SuccessStatus.CARD_DISMISS_SUCCESS, response);
    }

    @DeleteMapping("/{deckId}/cards/{cardId}/dismiss")
    public ResponseEntity<ApiResponse<CardResponse>> cancelDismissCard(
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CardResponse response = cardService.undismissCard(deckId, cardId, Long.valueOf(jwt.getSubject()));
        return ApiResponse.success(SuccessStatus.CARD_DISMISS_CANCEL_SUCCESS, response);
    }

    @PostMapping("/{deckId}/cards/{cardId}/dismiss-reason")
    public ResponseEntity<ApiResponse<Void>> submitDismissReason(
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DismissReasonRequest request
    ) {
        cardService.submitDismissReason(deckId, cardId, Long.valueOf(jwt.getSubject()), request);
        return ApiResponse.success(SuccessStatus.CARD_DISMISS_REASON_SUCCESS);
    }
}
