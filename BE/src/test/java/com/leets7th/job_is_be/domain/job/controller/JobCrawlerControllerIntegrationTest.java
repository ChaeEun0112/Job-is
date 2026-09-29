package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.service.JobCrawlerManager;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobCrawlerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @MockitoBean
    private JobCrawlerManager jobCrawlerManager;

    @Test
    void rejectsNonAdminUser() throws Exception {
        String accessToken = tokenProvider.issueTokenPair(1L, "USER").accessToken();

        mockMvc.perform(post("/api/admin/crawler/run")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());

        verify(jobCrawlerManager, never()).runPipelineAndSave();
    }

    @Test
    void allowsAdminUser() throws Exception {
        String accessToken = tokenProvider.issueTokenPair(1L, "ADMIN").accessToken();

        mockMvc.perform(post("/api/admin/crawler/run")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        verify(jobCrawlerManager).runPipelineAndSave();
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/admin/crawler/run"))
                .andExpect(status().isUnauthorized());

        verify(jobCrawlerManager, never()).runPipelineAndSave();
    }
}
