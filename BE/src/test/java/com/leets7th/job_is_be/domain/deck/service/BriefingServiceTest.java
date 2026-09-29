package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.BriefingResponse;
import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.personality.entity.PersonalityTest;
import com.leets7th.job_is_be.domain.personality.repository.PersonalityTestRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BriefingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private DeckRepository deckRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardService cardService;
    @Mock
    private PersonalityTestRepository personalityTestRepository;

    @InjectMocks
    private BriefingService briefingService;

    @Test
    void 사용자가_없으면_예외를_던진다() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> briefingService.getTodayBriefing(1L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 성향_퀴즈가_없으면_온보딩_미완_상태를_반환한다() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(jobRepository.countApplicable(any(), any())).thenReturn(847L);
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(personalityTestRepository.findFirstByUserIdAndCompletedTrueOrderByStartedAtDesc(1L))
                .thenReturn(Optional.empty());

        BriefingResponse response = briefingService.getTodayBriefing(1L);

        assertThat(response.curatedCount()).isEqualTo(0);
        assertThat(response.applicableCount()).isEqualTo(847L);
        assertThat(response.state()).isEqualTo("onboarding_incomplete");
        assertThat(response.deckId()).isNull();
        assertThat(response.theme()).isNull();
    }

    @Test
    void 퀴즈는_풀었지만_덱이_아직_없으면_준비_전_상태를_반환한다() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(jobRepository.countApplicable(any(), any())).thenReturn(847L);
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(personalityTestRepository.findFirstByUserIdAndCompletedTrueOrderByStartedAtDesc(1L))
                .thenReturn(Optional.of(PersonalityTest.builder().build()));

        BriefingResponse response = briefingService.getTodayBriefing(1L);

        assertThat(response.state()).isEqualTo("pre_slot");
    }

    @Test
    void 덱이_있으면_카드_수와_최빈_직무_테마를_반환한다() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(jobRepository.countApplicable(any(), any())).thenReturn(847L);

        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        Deck deck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(deck));

        JobCategory dataCategory = JobCategory.builder().name("데이터 분석가").build();
        JobCategory backendCategory = JobCategory.builder().name("백엔드 엔지니어").build();

        Job job1 = Job.builder().jobCategory(dataCategory).title("t1").build();
        Job job2 = Job.builder().jobCategory(dataCategory).title("t2").build();
        Job job3 = Job.builder().jobCategory(backendCategory).title("t3").build();

        List<Card> cards = List.of(
                Card.builder().deck(deck).job(job1).position(1).build(),
                Card.builder().deck(deck).job(job2).position(2).build(),
                Card.builder().deck(deck).job(job3).position(3).build()
        );
        when(cardRepository.findByDeckId(any())).thenReturn(cards);

        BriefingResponse response = briefingService.getTodayBriefing(1L);

        assertThat(response.curatedCount()).isEqualTo(3);
        assertThat(response.theme()).isEqualTo("오늘은 데이터 분석가 직무 위주로 골랐습니다.");
        assertThat(response.state()).isNull();
        assertThat(response.firstOpenedAt()).isNull();
        assertThat(deck.getFirstOpenedAt()).isNotNull();
    }

    @Test
    void 브리핑_현황_조회시_오늘_덱이_없으면_빈_목록을_반환한다() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThat(briefingService.getTodayBriefingStatus(1L)).isEmpty();
    }

    @Test
    void 브리핑_현황_조회시_오늘_덱의_카드_목록을_반환한다() {
        when(userRepository.existsById(1L)).thenReturn(true);

        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        Deck deck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(deck));

        CardResponse card = new CardResponse(1L, 2L, null, null, "주니어 백엔드 엔지니어", "추천 이유", null,
                List.of("Java"), "클라우드 데이터 플랫폼", "서울", List.of("신입"), null, "요약", 1, null);
        when(cardService.getDeckCards(deck.getId(), 1L)).thenReturn(List.of(card));

        List<CardResponse> response = briefingService.getTodayBriefingStatus(1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).jobTitle()).isEqualTo("주니어 백엔드 엔지니어");
    }
}
