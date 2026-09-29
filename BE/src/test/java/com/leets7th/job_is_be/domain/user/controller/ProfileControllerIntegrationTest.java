package com.leets7th.job_is_be.domain.user.controller;

import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.entity.Region;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.domain.job.repository.TechStackRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserTechStackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JobCategoryRepository jobCategoryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private TechStackRepository techStackRepository;
    @Autowired
    private UserTechStackRepository userTechStackRepository;

    private Long userId;
    private List<Long> jobCategoryIds;
    private List<Long> regionIds;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .socialId("profile-test-social-id")
                .socialType(SocialType.KAKAO)
                .email("profile-test@example.com")
                .build());
        userId = user.getId();

        jobCategoryIds = new ArrayList<>();
        for (int index = 1; index <= 4; index++) {
            JobCategory category = jobCategoryRepository.save(JobCategory.builder()
                    .name("직무 " + index)
                    .sortOrder(index)
                    .build());
            jobCategoryIds.add(category.getId());
        }
        // 희망 지역은 1~3개 다중 선택이라 검증하려면 마스터가 여러 건 필요하다(PRO-01 필드 표)
        regionIds = new ArrayList<>();
        String[] regionNames = {"서울", "경기", "인천"};
        for (int index = 0; index < regionNames.length; index++) {
            Region region = regionRepository.save(Region.builder()
                    .name(regionNames[index])
                    .sortOrder(index + 1)
                    .build());
            regionIds.add(region.getId());
        }
    }

    @Test
    void supportsDraftCompletionQueryAndUpdateFlow() throws Exception {
        mockMvc.perform(get("/api/profile/draft").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROFILE_200_3"))
                .andExpect(jsonPath("$.data").doesNotExist());

        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "jobCategoryIds": [%d],
                                  "primaryJobCategoryId": %d,
                                  "regionId": null,
                                  "remoteOk": null,
                                  "careerLevel": null,
                                  "preferenceNotes": ["원격 가능"],
                                  "excludeKeywords": [],
                                  "techStacks": ["Java"]
                                }
                                """.formatted(jobCategoryIds.get(0), jobCategoryIds.get(0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingStep").value("PROFILE"))
                .andExpect(jsonPath("$.data.region").doesNotExist())
                .andExpect(jsonPath("$.data.remoteOk").value(false))
                .andExpect(jsonPath("$.data.careerLevel").doesNotExist());

        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "REVIEW",
                                  "jobCategoryIds": [%d, %d],
                                  "primaryJobCategoryId": %d,
                                  "regionId": %d,
                                  "remoteOk": true,
                                  "careerLevel": "ENTRY",
                                  "preferenceNotes": ["원격 가능", "정규직 우선"],
                                  "excludeKeywords": ["야간 근무"],
                                  "techStacks": ["Java", "Spring"]
                                }
                                """.formatted(
                                jobCategoryIds.get(0),
                                jobCategoryIds.get(1),
                                jobCategoryIds.get(0),
                                regionIds.get(0)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingStep").value("REVIEW"))
                .andExpect(jsonPath("$.data.jobCategories.length()").value(2))
                .andExpect(jsonPath("$.data.jobCategories[0].primary").value(true))
                .andExpect(jsonPath("$.data.region.name").value("서울"))
                .andExpect(jsonPath("$.data.remoteOk").value(true));

        mockMvc.perform(post("/api/profile/onboarding/complete").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROFILE_200_5"));

        mockMvc.perform(get("/api/profile").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.preferenceNotes[0]").value("원격 가능"));

        mockMvc.perform(patch("/api/profile")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "preferenceNotes": [],
                                  "excludeKeywords": ["주말 근무"],
                                  "techStacks": ["Kotlin"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROFILE_200_2"))
                .andExpect(jsonPath("$.data.preferenceNotes").isEmpty())
                .andExpect(jsonPath("$.data.excludeKeywords[0]").value("주말 근무"))
                .andExpect(jsonPath("$.data.techStacks[0]").value("Kotlin"));
    }

    @Test
    void rejectsCompletionWhenRequiredValuesAreMissing() throws Exception {
        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "REVIEW",
                                  "jobCategoryIds": [],
                                  "primaryJobCategoryId": null,
                                  "regionId": null,
                                  "careerLevel": null
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/profile/onboarding/complete").with(userJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROFILE_400_5"));
    }

    @Test
    void rejectsMoreThanThreeJobCategories() throws Exception {
        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "jobCategoryIds": [%d, %d, %d, %d],
                                  "primaryJobCategoryId": %d
                                }
                                """.formatted(
                                jobCategoryIds.get(0),
                                jobCategoryIds.get(1),
                                jobCategoryIds.get(2),
                                jobCategoryIds.get(3),
                                jobCategoryIds.get(0)
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROFILE_400_2"));
    }

    @Test
    void rejectsUnknownRegion() throws Exception {
        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "regionId": 999999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_404_3"));
    }

    @Test
    void replacesRegionWhenSelectedAgain() throws Exception {
        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "regionId": %d
                                }
                                """.formatted(regionIds.get(0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.region.name").value("서울"));

        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "regionId": %d
                                }
                                """.formatted(regionIds.get(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.region.name").value("경기"));
    }

    @Test
    void preservesExistingDraftValuesWhenLaterStepOmitsThem() throws Exception {
        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "jobCategoryIds": [%d],
                                  "primaryJobCategoryId": %d,
                                  "regionId": %d,
                                  "careerLevel": "ENTRY",
                                  "preferenceNotes": ["remote"],
                                  "excludeKeywords": ["night shift"],
                                  "techStacks": ["Java"]
                                }
                                """.formatted(jobCategoryIds.get(0), jobCategoryIds.get(0), regionIds.get(0))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "QUIZ"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingStep").value("QUIZ"))
                .andExpect(jsonPath("$.data.jobCategories.length()").value(1))
                .andExpect(jsonPath("$.data.jobCategories[0].id").value(jobCategoryIds.get(0)))
                .andExpect(jsonPath("$.data.region.id").value(regionIds.get(0)))
                .andExpect(jsonPath("$.data.careerLevel").value("ENTRY"))
                .andExpect(jsonPath("$.data.preferenceNotes[0]").value("remote"))
                .andExpect(jsonPath("$.data.excludeKeywords[0]").value("night shift"))
                .andExpect(jsonPath("$.data.techStacks[0]").value("Java"));
    }

    @Test
    void profileEndpointRejectsIncompleteDraft() throws Exception {
        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "jobCategoryIds": [],
                                  "primaryJobCategoryId": null
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/profile").with(userJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_404_1"));
    }

    @Test
    void normalizesAndReplacesUserTechStacks() throws Exception {
        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "techStacks": [" Java ", "java", "Spring Boot"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.techStacks.length()").value(2))
                .andExpect(jsonPath("$.data.techStacks[0]").value("Java"))
                .andExpect(jsonPath("$.data.techStacks[1]").value("Spring Boot"));

        assertThat(techStackRepository.findByNormalizedName("java")).isPresent();
        assertThat(userTechStackRepository.findAllByUserIdOrderByIdAsc(userId))
                .extracting(userTechStack -> userTechStack.getTechStack().getName())
                .containsExactly("Java", "Spring Boot");

        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "QUIZ",
                                  "techStacks": ["Kotlin"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.techStacks.length()").value(1))
                .andExpect(jsonPath("$.data.techStacks[0]").value("Kotlin"));

        assertThat(userTechStackRepository.findAllByUserIdOrderByIdAsc(userId))
                .extracting(userTechStack -> userTechStack.getTechStack().getName())
                .containsExactly("Kotlin");
    }

    @Test
    void rejectsBlankTechStackName() throws Exception {
        mockMvc.perform(put("/api/profile/draft")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "onboardingStep": "PROFILE",
                                  "techStacks": [" "]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROFILE_400_6"));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor userJwt() {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }
}
