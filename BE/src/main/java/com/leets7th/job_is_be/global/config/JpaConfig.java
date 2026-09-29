package com.leets7th.job_is_be.global.config;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// BaseEntity의 @CreatedDate, @LastModifiedDate 활성화
// createdAt/updatedAt이 OffsetDateTime이라 기본 DateTimeProvider(LocalDateTime 반환)로는 변환이 안 되어 직접 지정
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "offsetDateTimeProvider")
public class JpaConfig {

    @Bean
    public DateTimeProvider offsetDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
