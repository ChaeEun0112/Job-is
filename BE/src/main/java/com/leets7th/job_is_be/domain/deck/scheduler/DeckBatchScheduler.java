package com.leets7th.job_is_be.domain.deck.scheduler;

import com.leets7th.job_is_be.domain.deck.service.RecommendationService;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeckBatchScheduler {

    private final UserRepository userRepository;
    private final RecommendationService recommendationService;

    // 매일 06:00 — 전체 사용자 덱 자동 생성 (REC §0.5)
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    public void generateDailyDecks() {
        var userIds = userRepository.findAll().stream()
                .map(u -> u.getId())
                .toList();
        log.info("[DeckBatch] 배치 시작 — 대상 {} 명", userIds.size());

        int success = 0;
        int failure = 0;
        for (Long userId : userIds) {
            try {
                recommendationService.generateTodayDeck(userId);
                success++;
            } catch (Exception e) {
                log.warn("[DeckBatch] userId={} 덱 생성 실패: {}", userId, e.getMessage());
                failure++;
            }
        }

        log.info("[DeckBatch] 배치 완료 — 성공 {}, 실패 {}", success, failure);
    }
}
