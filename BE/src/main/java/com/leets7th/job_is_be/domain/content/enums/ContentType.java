package com.leets7th.job_is_be.domain.content.enums;

import lombok.Getter;

@Getter
public enum ContentType {

    NEWS("뉴스"),
    BENEFIT("혜택");

    private final String label;

    ContentType(String label) {
        this.label = label;
    }
}
