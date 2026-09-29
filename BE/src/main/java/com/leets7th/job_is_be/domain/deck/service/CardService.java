package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.dto.DismissReasonRequest;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import com.leets7th.job_is_be.domain.deck.enums.DeckItemStatus;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.deck.repository.UserActionRepository;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final UserActionRepository userActionRepository;

    // 추천 파이프라인(recall/precision)이 채운 덱의 카드 목록을 그대로 조회
    public List<CardResponse> getDeckCards(Long deckId, Long userId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.DECK_NOT_FOUND));
        if (!deck.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        return cardRepository.findByDeckId(deckId).stream()
                .map(card -> CardResponse.from(card, resolveTechStack(card.getJob())))
                .toList();
    }

    private List<String> resolveTechStack(Job job) {
        if (job == null || job.getSkillTags() == null) {
            return List.of();
        }
        return job.getSkillTags();
    }

    // 관심없음: 카드 상태를 DISMISSED로 전환하고 이벤트 로그를 남김
    @Transactional
    public CardResponse dismissCard(Long deckId, Long cardId, Long userId) {
        Card card = getCardInDeck(deckId, cardId, userId);
        if (card.getStatus() == DeckItemStatus.DISMISSED) {
            throw new GeneralException(ErrorStatus.CARD_ALREADY_DISMISSED);
        }
        card.dismiss();
        userActionRepository.save(UserAction.builder()
                .user(card.getDeck().getUser())
                .job(card.getJob())
                .actionType(ActionType.DISMISSED)
                .build());
        return CardResponse.from(card, resolveTechStack(card.getJob()));
    }

    // 관심없음 취소: 카드 상태를 PENDING으로 되돌림 (1번과 토글 관계, 로그는 남기지 않음)
    @Transactional
    public CardResponse undismissCard(Long deckId, Long cardId, Long userId) {
        Card card = getCardInDeck(deckId, cardId, userId);
        if (card.getStatus() != DeckItemStatus.DISMISSED) {
            throw new GeneralException(ErrorStatus.CARD_NOT_DISMISSED);
        }
        card.undismiss();
        return CardResponse.from(card, resolveTechStack(card.getJob()));
    }

    // 관심없음 사유 제출: 카드 상태는 건드리지 않고 사유 이벤트 로그만 별도로 추가
    @Transactional
    public void submitDismissReason(Long deckId, Long cardId, Long userId, DismissReasonRequest request) {
        Card card = getCardInDeck(deckId, cardId, userId);
        if (card.getStatus() != DeckItemStatus.DISMISSED) {
            throw new GeneralException(ErrorStatus.CARD_NOT_DISMISSED);
        }
        if (card.isReasonSubmitted()) {
            throw new GeneralException(ErrorStatus.CARD_DISMISS_REASON_ALREADY_SUBMITTED);
        }
        userActionRepository.save(UserAction.builder()
                .user(card.getDeck().getUser())
                .job(card.getJob())
                .actionType(ActionType.DISMISSED)
                .reasonCode(request.reason().name())
                .comment(request.comment())
                .build());
        try {
            card.markReasonSubmitted();
            cardRepository.saveAndFlush(card);
        } catch (OptimisticLockingFailureException e) {
            // 동시 요청이 먼저 사유를 제출해 version이 이미 바뀐 경우: 이미 제출된 것으로 간주
            throw new GeneralException(ErrorStatus.CARD_DISMISS_REASON_ALREADY_SUBMITTED);
        }
    }

    private Card getCardInDeck(Long deckId, Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CARD_NOT_FOUND));
        if (!card.getDeck().getId().equals(deckId)) {
            throw new GeneralException(ErrorStatus.CARD_NOT_FOUND);
        }
        if (!card.getDeck().getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }
        return card;
    }
}
