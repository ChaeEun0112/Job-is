package com.leets7th.job_is_be.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "crawler")
public class CrawlerProperties {
    private String pythonPath;
    private String scriptPath;
    private String companyOutputPath;
    private String jobOutputPath;
}
