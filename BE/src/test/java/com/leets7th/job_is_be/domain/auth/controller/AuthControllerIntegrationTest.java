package com.leets7th.job_is_be.domain.auth.controller;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reissueWithoutRefreshCookieReturnsUnauthorized() throws Exception {
        CsrfCredentials csrf = issueCsrfToken();

        mockMvc.perform(post("/api/auth/token/reissue")
                        .cookie(csrf.cookie())
                        .header(csrf.headerName(), csrf.token()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_401_1"));
    }

    @Test
    void logoutWithoutRefreshCookieClearsCookie() throws Exception {
        CsrfCredentials csrf = issueCsrfToken();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(csrf.cookie())
                        .header(csrf.headerName(), csrf.token()))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH_200_3"));
    }

    @Test
    void meWithoutAccessTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMM_401"));
    }

    @Test
    void csrfEndpointIssuesTokenCookie() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH_200_4"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.headerName").value("X-XSRF-TOKEN"));
    }

    @Test
    void reissueWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/token/reissue"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMM_403"));
    }

    @Test
    void logoutWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMM_403"));
    }

    @Test
    void oauthExchangeDoesNotRequireCsrfToken() throws Exception {
        mockMvc.perform(post("/api/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_400_5"));
    }

    @Test
    void oauthExchangeRejectsFormContentType() throws Exception {
        mockMvc.perform(post("/api/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginCode", "login-code"))
                .andExpect(status().isUnsupportedMediaType());
    }

    private CsrfCredentials issueCsrfToken() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        return new CsrfCredentials(
                result.getResponse().getCookie("XSRF-TOKEN"),
                JsonPath.read(response, "$.data.token"),
                JsonPath.read(response, "$.data.headerName")
        );
    }

    private record CsrfCredentials(Cookie cookie, String token, String headerName) {
    }
}
