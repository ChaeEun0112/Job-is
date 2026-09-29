package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.BriefingResponse;
import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.enums.DeckState;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.personality.repository.PersonalityTestRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BriefingService {

    private static final String MORNING_SLOT = "07:30";
    private static final String LUNCH_SLOT = "12:30";
    private static final String EVENING_SLOT = "18:30";

    private static final LocalTime LUNCH_CUTOFF = LocalTime.of(10, 0);
    private static final LocalTime EVENING_CUTOFF = LocalTime.of(15, 30);

    // 덱 생성 배치 SLA — 매일 06:00 완료 (REC §0.5)
    private static final LocalTime DECK_READY_TIME = LocalTime.of(6, 0);

    private static final String DEFAULT_THEME = "오늘의 맞춤 공고를 준비했습니다.";

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final PersonalityTestRepository personalityTestRepository;

    /**
     * REC-01 인트로 / REC-07 빈 상태 — 덱 메타와 빈 상태 원인을 함께 내려준다(REC §0.3).
     * 덱이 없어도 예외가 아니라 state 코드가 담긴 정상 응답이다.
     * 최초 열람 시각을 여기서 기록하므로 쓰기 트랜잭션이다.
     */
    @Transactional
    public BriefingResponse getTodayBriefing(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        OffsetDateTime now = OffsetDateTime.now();
        LocalDate today = now.toLocalDate();
        String slot = resolveSlot(now.toLocalTime());
        String greeting = resolveGreeting(slot);

        // 지원가능 건수 — 마감되지 않은 전체 공고 수 조회
        long applicableCount = jobRepository.countApplicable(JobStatus.ACTIVE, now);

        // 오늘의 Deck 조회
        Optional<Deck> deck = deckRepository.findByUserIdAndDeckDate(userId, today);
        List<Card> cards = deck.map(d -> cardRepository.findByDeckId(d.getId()))
                .orElse(List.of());

        // 최초 열람 시각 기록 전에 현재 값을 읽어야 첫 방문/재방문 판정이 뒤집히지 않는다(REC §0.4)
        OffsetDateTime firstOpenedAt = deck.map(Deck::getFirstOpenedAt).orElse(null);
        deck.ifPresent(d -> d.markFirstOpened(now));

        DeckState state = resolveState(userId, deck.orElse(null), cards, now);

        Long deckSeq = null;
        if (deck.isPresent()) {
            deckSeq = deckRepository.countByUserIdAndDeckDateLessThanEqual(userId, today);
        }

        return new BriefingResponse(
                deck.map(Deck::getId).orElse(null),
                today,
                deckSeq,
                state != null ? state.getCode() : null,
                firstOpenedAt,
                greeting,
                applicableCount,
                cards.size(),
                cards.isEmpty() ? null : resolveTheme(cards)
        );
    }

    /**
     * REC-02 브리핑 덱 — 오늘 덱 카드 목록 조회.
     * 덱이 없으면 빈 목록을 돌려준다(빈 상태 원인은 {@link #getTodayBriefing} 의 state 로 판정, REC §0.3).
     */
    public List<CardResponse> getTodayBriefingStatus(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        return deckRepository.findByUserIdAndDeckDate(userId, OffsetDateTime.now().toLocalDate())
                .map(deck -> cardService.getDeckCards(deck.getId(), userId))
                .orElse(List.of());
    }

    /**
     * 빈 상태 원인 판정(REC-07). 카드가 1건이라도 있으면 정상 덱이므로 null.
     * 프런트 조합 판정을 막기 위해 서버가 단일 코드로 확정한다.
     */
    private DeckState resolveState(Long userId, Deck deck, List<Card> cards, OffsetDateTime now) {
        if (!cards.isEmpty()) {
            return null;
        }

        // 덱 생성 시점에 기록해둔 원인이 있으면 그대로 사용
        if (deck != null) {
            DeckState recorded = DeckState.fromCode(deck.getEmptyReason());
            if (recorded != null) {
                return recorded;
            }
        }

        // 성향 퀴즈 미완료 = 첫 레터를 만들 근거가 없는 상태
        if (personalityTestRepository.findFirstByUserIdAndCompletedTrueOrderByStartedAtDesc(userId).isEmpty()) {
            return DeckState.ONBOARDING_INCOMPLETE;
        }

        // 덱 자체가 아직 없으면 배치 전(준비 전), 덱은 있는데 카드가 0건이면 후보 부족일
        if (deck == null) {
            return DeckState.PRE_SLOT;
        }
        return now.toLocalTime().isBefore(DECK_READY_TIME) ? DeckState.PRE_SLOT : DeckState.NO_CANDIDATES;
    }

    // 시간대 별 인사문구 추출
    private String resolveSlot(LocalTime now) {
        if (now.isBefore(LUNCH_CUTOFF)) {
            return MORNING_SLOT;
        }
        if (now.isBefore(EVENING_CUTOFF)) {
            return LUNCH_SLOT;
        }
        return EVENING_SLOT;
    }

    // 시간대 별 인사문구
    private String resolveGreeting(String slot) {
        return switch (slot) {
            case MORNING_SLOT -> "⛅ 좋은 아침입니다";
            case LUNCH_SLOT -> "🌞 즐거운 점심 시간입니다";
            default -> "🌙 좋은 저녁입니다";
        };
    }

    // 테마 + 로더 — 큐레이션 테마 한 줄과 로딩 문구 추출
    private String resolveTheme(List<Card> cards) {
        Map<String, Long> categoryCounts = cards.stream()
                .filter(card -> card.getJob() != null && card.getJob().getJobCategory() != null)
                .map(card -> card.getJob().getJobCategory())
                .collect(Collectors.groupingBy(JobCategory::getName, Collectors.counting()));

        return categoryCounts.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> "오늘은 " + entry.getKey() + " 직무 위주로 골랐습니다.")
                .orElse(DEFAULT_THEME);
    }
}
