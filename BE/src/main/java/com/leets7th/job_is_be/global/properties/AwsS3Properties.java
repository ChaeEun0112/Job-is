package com.leets7th.job_is_be.global.properties;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.aws.s3")
public record AwsS3Properties(
        @NotBlank String bucket,
        @NotBlank String region,
        @DurationMin(seconds = 1) Duration presignedUrlExpiration
) {
}
