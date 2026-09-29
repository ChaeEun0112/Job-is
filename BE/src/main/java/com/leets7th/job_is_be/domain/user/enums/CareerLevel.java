package com.leets7th.job_is_be.domain.user.enums;

import lombok.Getter;

@Getter
public enum CareerLevel {
    ENTRY("신입"),
    JUNIOR("주니어"),
    EXPERIENCED ("시니어");

    private final String description;

    // 생성자 직접 작성
    CareerLevel(String description) {
        this.description = description;
    }
}
