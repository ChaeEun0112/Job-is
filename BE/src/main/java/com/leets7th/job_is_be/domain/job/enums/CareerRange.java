package com.leets7th.job_is_be.domain.job.enums;

/**
 * 탐색 경력 필터 구간 (화면설계서 EXP §4.2).
 * NEWCOMER : is_newbie=true 또는 career_min=0
 * JUNIOR   : career_min<=3 AND career_max>=1 (구간 겹침)
 * SENIOR   : career_max>=4
 */
public enum CareerRange {
    NEWCOMER,
    JUNIOR,
    SENIOR
}
