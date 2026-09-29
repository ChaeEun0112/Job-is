package com.leets7th.job_is_be.domain.personality.controller;

import com.leets7th.job_is_be.domain.personality.entity.PersonalityTest;
import com.leets7th.job_is_be.domain.personality.repository.PersonalityTestRepository;
import com.leets7th.job_is_be.domain.personality.service.PersonalityTagCodec;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserProfile;
import com.leets7th.job_is_be.domain.user.enums.OnboardingStep;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PersonalityQuizControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private PersonalityTestRepository personalityTestRepository;
    @Autowired
    private PersonalityTagCodec tagCodec;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .socialId("quiz-test-social")
                .socialType(SocialType.KAKAO)
                .email("quiz-test@example.com")
                .build());
        userId = user.getId();
        userProfileRepository.save(UserProfile.builder()
                .user(user)
                .onboardingStep(OnboardingStep.QUIZ)
                .build());
    }

    @Test
    void supportsQuestionAnswerResultAndApplyFlow() throws Exception {
        mockMvc.perform(get("/api/quiz/questions")
                        .queryParam("source", "ONBOARDING")
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("QUIZ_200_1"))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.questions.length()").value(10));

        PersonalityTest test = personalityTestRepository.findAll().getFirst();

        saveAnswer(test.getId(), 1, 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answeredCount").value(1));
        saveAnswer(test.getId(), 1, 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answeredCount").value(1));

        for (int questionNo = 2; questionNo <= 10; questionNo++) {
            saveAnswer(test.getId(), questionNo, 2)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.completed").value(questionNo == 10));
        }

        mockMvc.perform(get("/api/quiz/result")
                        .queryParam("testId", test.getId().toString())
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("QUIZ_200_3"))
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.scores.stabilityChallenge").value(4))
                .andExpect(jsonPath("$.data.scores.balanceImmersion").value(4))
                .andExpect(jsonPath("$.data.scores.expertAllRounder").value(2))
                .andExpect(jsonPath("$.data.resultType.code").value("CF"))
                .andExpect(jsonPath("$.data.resultTags[0]").value("도전지향"))
                .andExpect(jsonPath("$.data.resultTags[1]").value("성장지향"))
                .andExpect(jsonPath("$.data.resultTags[2]").value("올라운더형"));

        mockMvc.perform(post("/api/quiz/result/apply")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "testId": %d
                                }
                                """.formatted(test.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("QUIZ_200_4"))
                .andExpect(jsonPath("$.data.resultType").value("CF"))
                .andExpect(jsonPath("$.data.jobTestCompleted").value(true));

        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow();
        assertThat(profile.isJobTestCompleted()).isTrue();
        assertThat(profile.getOnboardingStep()).isEqualTo(OnboardingStep.REVIEW);
        assertThat(tagCodec.decode(profile.getPersonalityTags()))
                .containsExactly("도전지향", "성장지향", "올라운더형");

        mockMvc.perform(get("/api/profile/draft").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.personalityTags[0]").value("도전지향"))
                .andExpect(jsonPath("$.data.jobTestCompleted").value(true));
    }

    @Test
    void returnsProgressAndRejectsApplyingIncompleteTest() throws Exception {
        mockMvc.perform(get("/api/quiz/questions").with(userJwt()))
                .andExpect(status().isOk());
        PersonalityTest test = personalityTestRepository.findAll().getFirst();

        saveAnswer(test.getId(), 1, 1).andExpect(status().isOk());

        mockMvc.perform(get("/api/quiz/result")
                        .queryParam("testId", test.getId().toString())
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(false))
                .andExpect(jsonPath("$.data.answeredCount").value(1))
                .andExpect(jsonPath("$.data.scores").doesNotExist());

        mockMvc.perform(post("/api/quiz/result/apply")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "testId": %d
                                }
                                """.formatted(test.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("QUIZ_409_1"));
    }

    @Test
    void createsDraftProfileWhenApplyingOnboardingResultWithoutProfile() throws Exception {
        userProfileRepository.deleteAll();
        userProfileRepository.flush();

        mockMvc.perform(get("/api/quiz/questions")
                        .queryParam("source", "ONBOARDING")
                        .with(userJwt()))
                .andExpect(status().isOk());
        PersonalityTest test = personalityTestRepository.findAll().getFirst();

        for (int questionNo = 1; questionNo <= 10; questionNo++) {
            saveAnswer(test.getId(), questionNo, 2)
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/quiz/result/apply")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "testId": %d
                                }
                                """.formatted(test.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("QUIZ_200_4"))
                .andExpect(jsonPath("$.data.jobTestCompleted").value(true));

        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow();
        assertThat(profile.isOnboardingCompleted()).isFalse();
        assertThat(profile.isJobTestCompleted()).isTrue();
        assertThat(profile.getOnboardingStep()).isEqualTo(OnboardingStep.REVIEW);
    }

    @Test
    void rejectsProfileRetestResultWhenProfileDoesNotExist() throws Exception {
        userProfileRepository.deleteAll();
        userProfileRepository.flush();

        mockMvc.perform(get("/api/quiz/questions")
                        .queryParam("source", "PROFILE")
                        .with(userJwt()))
                .andExpect(status().isOk());
        PersonalityTest test = personalityTestRepository.findAll().getFirst();

        for (int questionNo = 1; questionNo <= 10; questionNo++) {
            saveAnswer(test.getId(), questionNo, 2)
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/quiz/result/apply")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "testId": %d
                                }
                                """.formatted(test.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_404_1"));
    }

    private org.springframework.test.web.servlet.ResultActions saveAnswer(
            Long testId,
            int questionNo,
            int choiceValue
    ) throws Exception {
        return mockMvc.perform(post("/api/quiz/answers")
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "testId": %d,
                          "questionNo": %d,
                          "choiceValue": %d
                        }
                        """.formatted(testId, questionNo, choiceValue)));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor userJwt() {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }
}
