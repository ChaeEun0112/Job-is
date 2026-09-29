package com.leets7th.job_is_be.domain.mail.service;

import com.leets7th.job_is_be.domain.mail.enums.MailType;
import com.leets7th.job_is_be.domain.mail.gateway.MailGateway;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MailDispatchServiceTest {

    private final MailDeliveryLogService logService = mock(MailDeliveryLogService.class);
    private final MailGateway gateway = mock(MailGateway.class);
    private final MailDispatchService service = new MailDispatchService(logService, gateway);
    private final User user = User.builder()
            .socialId("social")
            .socialType(SocialType.GOOGLE)
            .email("user@example.com")
            .build();

    @Test
    void skipsAlreadyClaimedDelivery() {
        when(logService.claim(user, MailType.WELCOME, "key")).thenReturn(null);

        assertThat(service.send(user, MailType.WELCOME, "key", "subject", "html")).isFalse();

        verifyNoInteractions(gateway);
    }

    @Test
    void recordsSuccessfulDelivery() {
        when(logService.claim(user, MailType.WELCOME, "key")).thenReturn(1L);

        assertThat(service.send(user, MailType.WELCOME, "key", "subject", "html")).isTrue();

        verify(gateway).send("user@example.com", "subject", "html");
        verify(logService).markSent(1L);
    }

    @Test
    void recordsFailedDeliveryWithoutRethrowing() {
        when(logService.claim(user, MailType.WELCOME, "key")).thenReturn(1L);
        doThrow(new IllegalStateException("smtp error"))
                .when(gateway).send("user@example.com", "subject", "html");

        assertThat(service.send(user, MailType.WELCOME, "key", "subject", "html")).isFalse();

        verify(logService).markFailed(eq(1L), any(IllegalStateException.class));
    }
}
