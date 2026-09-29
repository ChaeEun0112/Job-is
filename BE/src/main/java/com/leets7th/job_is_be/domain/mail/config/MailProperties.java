package com.leets7th.job_is_be.domain.mail.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private boolean enabled;
    private String fromAddress;
    private String fromName = "Job.is";
    private String frontendBaseUrl = "http://localhost:5173";
    private String welcomePath = "/recommendations";
    private String briefingPath = "/recommendations/deck";
    private String profilePath = "/profile";
    private String notificationSettingsPath = "/settings/notifications";
    private String unsubscribePath = "/unsubscribe";
}
