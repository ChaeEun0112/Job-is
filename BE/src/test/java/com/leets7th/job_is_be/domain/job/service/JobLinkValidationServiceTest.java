package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.JobLinkValidateResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class JobLinkValidationServiceTest {

    private JobLinkValidationService jobLinkValidationService;
    private MockRestServiceServer server;

    @Mock
    private JobRepository jobRepository;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        jobLinkValidationService = new JobLinkValidationService(restClient, jobRepository);
    }

    private Job createJob(JobStatus status, String sourceUrl) {
        Job job = org.mockito.Mockito.mock(Job.class);
        given(job.getStatus()).willReturn(status);
        given(job.getSourceUrl()).willReturn(sourceUrl);
        return job;
    }

    @Test
    @DisplayName("정상 URL 접근 시 isValid가 true를 반환한다")
    void validateJobLink_Success() {
        // given
        String targetUrl = "https://example.com/job/1";
        Job job = createJob(JobStatus.ACTIVE, targetUrl);

        given(jobRepository.findById(1L)).willReturn(Optional.of(job));

        server.expect(requestTo(targetUrl))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess("", MediaType.TEXT_HTML));

        // when
        JobLinkValidateResponse response = jobLinkValidationService.validateJobLink(1L);

        // then
        assertThat(response.isValid()).isTrue();
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("404 URL 접근 시 isValid가 false를 반환하고 404 코드가 담긴다")
    void validateJobLink_404Error() {
        // given
        String targetUrl = "https://example.com/job/not-found";
        Job job = createJob(JobStatus.ACTIVE, targetUrl);

        given(jobRepository.findById(1L)).willReturn(Optional.of(job));

        server.expect(requestTo(targetUrl))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        server.expect(requestTo(targetUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // when
        JobLinkValidateResponse response = jobLinkValidationService.validateJobLink(1L);

        // then
        assertThat(response.isValid()).isFalse();
        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("타임아웃 발생 시 isValid가 false를 반환한다")
    void validateJobLink_Timeout() {
        // given
        String targetUrl = "https://example.com/job/timeout";
        Job job = createJob(JobStatus.ACTIVE, targetUrl);

        given(jobRepository.findById(1L)).willReturn(Optional.of(job));

        server.expect(requestTo(targetUrl))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        server.expect(requestTo(targetUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        // when
        JobLinkValidateResponse response = jobLinkValidationService.validateJobLink(1L);

        // then
        assertThat(response.isValid()).isFalse();
        assertThat(response.statusCode()).isNull();
    }
}
