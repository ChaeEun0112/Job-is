package com.leets7th.job_is_be.domain.job.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechStackType {

    // 테스트 용 임시
    // SQL 주석 예시 항목
    PYTHON("Python"),
    REACT("React"),
    JAVA("Java"),
    SPRING_BOOT("Spring Boot"),
    JAVASCRIPT("JavaScript"),
    TYPESCRIPT("TypeScript"),
    NODE_JS("Node.js"),
    VUE_JS("Vue.js"),
    NEXT_JS("Next.js"),
    KOTLIN("Kotlin"),
    GO("Go"),
    C_PLUS_PLUS("C++"),

    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    REDIS("Redis"),
    DOCKER("Docker"),
    AWS("AWS");

    private final String value;

}
