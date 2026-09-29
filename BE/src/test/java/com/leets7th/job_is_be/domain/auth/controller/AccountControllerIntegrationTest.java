package com.leets7th.job_is_be.domain.auth.controller;

import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserConsentRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserConsentRepository userConsentRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .socialId("account-test-social-id")
                .socialType(SocialType.KAKAO)
                .email("account-test@example.com")
                .build());
        userId = user.getId();
    }

    @Test
    void savesAndUpdatesConsent() throws Exception {
        mockMvc.perform(post("/api/auth/consent")
                        .with(jwt().jwt(token -> token.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "termsAgreed": true,
                                  "privacyAgreed": true,
                                  "ageOver14Agreed": true,
                                  "marketingAgreed": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("AUTH_200_7"));

        mockMvc.perform(post("/api/auth/consent")
                        .with(jwt().jwt(token -> token.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "termsAgreed": true,
                                  "privacyAgreed": true,
                                  "ageOver14Agreed": true,
                                  "marketingAgreed": true
                                }
                                """))
                .andExpect(status().isOk());

        var consent = userConsentRepository.findByUserId(userId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(consent.isMarketingAgreed()).isTrue();
        org.assertj.core.api.Assertions.assertThat(userConsentRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsMissingRequiredConsent() throws Exception {
        mockMvc.perform(post("/api/auth/consent")
                        .with(jwt().jwt(token -> token.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "termsAgreed": false,
                                  "privacyAgreed": true,
                                  "ageOver14Agreed": true,
                                  "marketingAgreed": false
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
