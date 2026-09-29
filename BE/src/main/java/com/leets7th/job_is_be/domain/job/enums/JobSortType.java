package com.leets7th.job_is_be.domain.job.enums;

/**
 * 탐색 정렬 옵션 (화면설계서 EXP §3.4).
 * FIT(추천순)은 적합도 점수 기준이나, 적합도 산출 파이프라인이 붙기 전까지는
 * §3.4의 "적합도 산출 불가 카드 = 맨 뒤 + 최신순 tie-break" 규칙에 따라 최신순으로 동작한다.
 */
public enum JobSortType {
    FIT,
    RECENT,
    DEADLINE
}
