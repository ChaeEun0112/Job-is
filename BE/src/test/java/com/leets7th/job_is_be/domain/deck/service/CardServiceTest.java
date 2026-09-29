package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.dto.DismissReasonRequest;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.enums.DismissReason;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.deck.repository.UserActionRepository;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private DeckRepository deckRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserActionRepository userActionRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void 덱이_없으면_예외를_던진다() {
        when(deckRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getDeckCards(1L, 1L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 덱의_카드_목록을_추천_이유와_함께_반환한다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Deck deck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        Job job = Job.builder()
                .title("백엔드 엔지니어 채용")
                .careerLevel("신입")
                .source("wanted")
                .externalId(10L)
                .skillTags(List.of("Java", "Spring"))
                .build();
        Card card = Card.builder()
                .deck(deck)
                .job(job)
                .position(1)
                .fitScore(new BigDecimal("92.50"))
                .reason("최근 관심 직무와 일치합니다")
                .summary("백엔드, 3년 이상, 서울")
                .build();
        when(cardRepository.findByDeckId(1L)).thenReturn(List.of(card));

        List<CardResponse> response = cardService.getDeckCards(1L, 1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).jobTitle()).isEqualTo("백엔드 엔지니어 채용");
        assertThat(response.get(0).reason()).isEqualTo("최근 관심 직무와 일치합니다");
        assertThat(response.get(0).summary()).isEqualTo("백엔드, 3년 이상, 서울");
        assertThat(response.get(0).techStack()).containsExactly("Java", "Spring");
        assertThat(response.get(0).tags()).containsExactly("신입", "연봉 비공개");
    }

    @Test
    void 이미_관심없음_처리된_카드를_다시_관심없음_처리하면_예외를_던진다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Deck deck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        ReflectionTestUtils.setField(deck, "id", 1L);
        Card card = Card.builder().position(1).build();
        ReflectionTestUtils.setField(card, "deck", deck);
        ReflectionTestUtils.setField(card, "id", 1L);
        card.dismiss();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.dismissCard(1L, 1L, 1L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 관심없음_처리되지_않은_카드의_관심없음을_취소하면_예외를_던진다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Deck deck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        ReflectionTestUtils.setField(deck, "id", 1L);
        Card card = Card.builder().position(1).build();
        ReflectionTestUtils.setField(card, "deck", deck);
        ReflectionTestUtils.setField(card, "id", 1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.undismissCard(1L, 1L, 1L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 이미_관심없음_사유를_제출한_카드에_다시_사유를_제출하면_예외를_던진다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Deck deck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        ReflectionTestUtils.setField(deck, "id", 1L);
        Card card = Card.builder().position(1).build();
        ReflectionTestUtils.setField(card, "deck", deck);
        ReflectionTestUtils.setField(card, "id", 1L);
        card.dismiss();
        card.markReasonSubmitted();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.submitDismissReason(1L, 1L, 1L, new DismissReasonRequest(DismissReason.JOB_MISMATCH, null)))
                .isInstanceOf(GeneralException.class);
    }
}
