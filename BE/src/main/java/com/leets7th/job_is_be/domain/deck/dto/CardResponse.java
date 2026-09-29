package com.leets7th.job_is_be.domain.deck.dto;

import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.enums.DeckItemStatus;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * REC-02 브리핑 덱 카드 응답 — 추천 파이프라인(recall/precision)이 채운 결과를 그대로 노출
 */
public record CardResponse(
        Long cardId,
        Long deckId,
        Long jobId,
        OffsetDateTime postedAt,   // 공고 올라온 날짜
        String jobTitle,          // 공고명
        String reason,            // 추천 이유
        BigDecimal fitScore,      // 적합도
        List<String> techStack,   // 기술 스택
        String jobScope,          // 공고 범위 (ex. 클라우드 데이터 플랫폼)
        String companyLocation,   // 회사 위치
        List<String> tags,        // 태그 (ex. 신입, 서울 강남, 연봉 비공개)
        OffsetDateTime deadlineAt, // 마감 일자 (NULL이면 상시)
        String summary,           // 한눈에 요약
        Integer position,
        DeckItemStatus status
) {
    public static CardResponse from(Card card, List<String> techStack) {
        Job job = card.getJob();
        Company company = job != null ? job.getCompany() : null;
        return new CardResponse(
                card.getId(),
                card.getDeck().getId(),
                job != null ? job.getId() : null,
                job != null ? job.getPostedAt() : null,
                job != null ? job.getTitle() : null,
                card.getReason(),
                card.getFitScore(),
                techStack,
                company != null ? company.getIndustry() : null,
                company != null ? company.getHqAddress() : null,
                job != null ? resolveTags(job) : List.of(),
                job != null ? job.getDeadlineAt() : null,
                card.getSummary(),
                card.getPosition(),
                card.getStatus()
        );
    }

    private static List<String> resolveTags(Job job) {
        List<String> tags = new ArrayList<>();
        if (job.getCareerLevel() != null) {
            tags.add(job.getCareerLevel());
        }
        String location = resolveLocationTag(job);
        if (location != null) {
            tags.add(location);
        }
        tags.add((job.getSalaryDisclosed() != null && job.getSalaryDisclosed()) ? "연봉 공개" : "연봉 비공개");
        return tags;
    }

    // Job.region(연관관계)은 크롤러가 못 채워 항상 비어 있어, 실제로 채워지는 평문 컬럼을 쓴다.
    private static String resolveLocationTag(Job job) {
        String city = job.getLocationCity();
        String district = job.getLocationDistrict();
        if (city != null && district != null) {
            return city + " " + district;
        }
        return city != null ? city : district;
    }
}
