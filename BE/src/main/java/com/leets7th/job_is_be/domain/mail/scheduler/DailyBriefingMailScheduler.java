package com.leets7th.job_is_be.domain.mail.scheduler;

import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.mail.enums.MailType;
import com.leets7th.job_is_be.domain.mail.service.MailDispatchService;
import com.leets7th.job_is_be.domain.mail.service.MailTemplateRenderer;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class DailyBriefingMailScheduler {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final NotificationSettingRepository notificationSettingRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final MailDispatchService dispatchService;
    private final MailTemplateRenderer templateRenderer;

    @Scheduled(cron = "${app.mail.daily-briefing-cron}", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void sendDailyBriefings() {
        LocalDate today = LocalDate.now(SEOUL);
        for (NotificationSetting setting : notificationSettingRepository.findAllActiveEmailSubscribers()) {
            if (isSnoozed(setting, today)) {
                continue;
            }
            Deck deck = deckRepository.findByUserIdAndDeckDate(setting.getUser().getId(), today).orElse(null);
            if (deck == null) {
                continue;
            }
            List<Card> cards = cardRepository.findByDeckId(deck.getId());
            if (cards.isEmpty()) {
                continue;
            }
            List<MailTemplateRenderer.BriefingItem> items = cards.stream()
                    .map(this::toItem)
                    .toList();
            dispatchService.send(
                    setting.getUser(),
                    MailType.DAILY_BRIEFING,
                    "DAILY_BRIEFING:" + setting.getUser().getId() + ":" + today,
                    "[Job.is] 오늘의 레터가 도착했어요",
                    templateRenderer.dailyBriefing(items, setting.getUnsubscribeToken())
            );
        }
    }

    private boolean isSnoozed(NotificationSetting setting, LocalDate today) {
        return setting.isSnoozeIndefinite()
                || setting.getSnoozeUntil() != null && setting.getSnoozeUntil().isAfter(today);
    }

    private MailTemplateRenderer.BriefingItem toItem(Card card) {
        Job job = card.getJob();
        Company company = job == null ? null : job.getCompany();
        int fitScore = card.getFitScore() == null
                ? 0
                : card.getFitScore().setScale(0, RoundingMode.HALF_UP).intValue();
        return new MailTemplateRenderer.BriefingItem(
                job == null ? "추천 공고" : job.getTitle(),
                company == null ? "회사 정보 없음" : company.getName(),
                fitScore,
                card.getReason() == null ? "프로필과 관심 조건을 바탕으로 추천했어요." : card.getReason(),
                job == null || job.getCareerLevel() == null ? "경력 무관" : job.getCareerLevel(),
                job == null || job.getLocationFull() == null ? "지역 정보 없음" : job.getLocationFull(),
                deadlineLabel(job),
                job == null ? List.of() : job.getSkillTags()
        );
    }

    private String deadlineLabel(Job job) {
        if (job == null || job.getDeadlineAt() == null) {
            return "상시";
        }
        LocalDate deadline = job.getDeadlineAt().atZoneSameInstant(SEOUL).toLocalDate();
        long days = ChronoUnit.DAYS.between(LocalDate.now(SEOUL), deadline);
        if (days < 0) {
            return "마감";
        }
        return days == 0 ? "D-Day" : "D-" + days;
    }
}
