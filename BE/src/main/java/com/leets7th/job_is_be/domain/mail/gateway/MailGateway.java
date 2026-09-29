package com.leets7th.job_is_be.domain.mail.gateway;

public interface MailGateway {

    void send(String recipient, String subject, String html);
}
