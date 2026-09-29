package com.leets7th.job_is_be.domain.mail.gateway;

import com.leets7th.job_is_be.domain.mail.config.MailProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class GmailSmtpMailGateway implements MailGateway {

    private final JavaMailSender mailSender;
    private final MailProperties properties;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Override
    public void send(String recipient, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            String fromAddress = properties.getFromAddress() == null || properties.getFromAddress().isBlank()
                    ? smtpUsername
                    : properties.getFromAddress();
            helper.setFrom(fromAddress, properties.getFromName());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("메일 발송에 실패했습니다.", exception);
        }
    }
}
