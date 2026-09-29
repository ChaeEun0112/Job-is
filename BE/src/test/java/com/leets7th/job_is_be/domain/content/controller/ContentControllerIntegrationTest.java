package com.leets7th.job_is_be.domain.content.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/contents"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMM_401"));
    }

    @Test
    void rejectsUnauthenticatedDetailRequest() throws Exception {
        mockMvc.perform(get("/api/contents/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMM_401"));
    }

    @Test
    void getsContentsInDisplayOrder() throws Exception {
        mockMvc.perform(get("/api/contents").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CONTENT_200_1"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].contentId").value(1))
                .andExpect(jsonPath("$.data[0].contentType").value("NEWS"))
                .andExpect(jsonPath("$.data[0].tag").value("뉴스"))
                .andExpect(jsonPath("$.data[1].contentId").value(2))
                .andExpect(jsonPath("$.data[1].contentType").value("BENEFIT"))
                .andExpect(jsonPath("$.data[1].tag").value("혜택"));
    }

    @Test
    void getsNewsDetailWithUnusedBenefitFieldsAsNull() throws Exception {
        mockMvc.perform(get("/api/contents/1").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CONTENT_200_2"))
                .andExpect(jsonPath("$.data.contentType").value("NEWS"))
                .andExpect(jsonPath("$.data.title").value("2026년 신입 개발자 채용 시장 리포트"))
                .andExpect(jsonPath("$.data.target").isEmpty())
                .andExpect(jsonPath("$.data.applicationStartDate").isEmpty())
                .andExpect(jsonPath("$.data.applicationEndDate").isEmpty())
                .andExpect(jsonPath("$.data.applicationMethod").isEmpty());
    }

    @Test
    void getsBenefitDetail() throws Exception {
        mockMvc.perform(get("/api/contents/2").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CONTENT_200_2"))
                .andExpect(jsonPath("$.data.contentType").value("BENEFIT"))
                .andExpect(jsonPath("$.data.target").value("신입 ~ 3년 차 개발자"))
                .andExpect(jsonPath("$.data.applicationStartDate").value("2026-07-01"))
                .andExpect(jsonPath("$.data.applicationEndDate").value("2026-07-31"))
                .andExpect(jsonPath("$.data.applicationMethod")
                        .value("잡코리아 파트너 페이지에서 신청서 작성"));
    }

    @Test
    void returnsNotFoundWhenContentDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/contents/999").with(userJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CONTENT_404_1"))
                .andExpect(jsonPath("$.message").value("뉴스/혜택 콘텐츠를 찾을 수 없습니다."));
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt() {
        return jwt().jwt(builder -> builder.subject("1"));
    }
}
