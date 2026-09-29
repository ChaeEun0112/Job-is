package com.leets7th.job_is_be.domain.user.service;

import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.entity.Region;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.domain.mail.event.OnboardingCompletedEvent;
import com.leets7th.job_is_be.domain.user.dto.ProfileDraftRequest;
import com.leets7th.job_is_be.domain.user.dto.ProfileDraftResponse;
import com.leets7th.job_is_be.domain.user.dto.ProfileJobCategoryResponse;
import com.leets7th.job_is_be.domain.user.dto.ProfileRegionResponse;
import com.leets7th.job_is_be.domain.user.dto.ProfileResponse;
import com.leets7th.job_is_be.domain.user.dto.ProfileUpdateRequest;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserJobCategory;
import com.leets7th.job_is_be.domain.user.entity.UserProfile;
import com.leets7th.job_is_be.domain.user.entity.UserRegion;
import com.leets7th.job_is_be.domain.user.repository.UserJobCategoryRepository;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRegionRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.enums.OnboardingStep;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final int MAX_JOB_CATEGORIES = 3;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserJobCategoryRepository userJobCategoryRepository;
    private final UserRegionRepository userRegionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final RegionRepository regionRepository;
    private final ProfileValueCodec valueCodec;
    private final UserTechStackService userTechStackService;
    private final ApplicationEventPublisher eventPublisher;

    public ProfileService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserJobCategoryRepository userJobCategoryRepository,
            UserRegionRepository userRegionRepository,
            JobCategoryRepository jobCategoryRepository,
            RegionRepository regionRepository,
            ProfileValueCodec valueCodec,
            UserTechStackService userTechStackService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userJobCategoryRepository = userJobCategoryRepository;
        this.userRegionRepository = userRegionRepository;
        this.jobCategoryRepository = jobCategoryRepository;
        this.regionRepository = regionRepository;
        this.valueCodec = valueCodec;
        this.userTechStackService = userTechStackService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ProfileDraftResponse saveDraft(Long userId, ProfileDraftRequest request) {
        if (request == null || request.onboardingStep() == null) {
            throw new GeneralException(ErrorStatus.PROFILE_ONBOARDING_STEP_REQUIRED);
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder()
                        .user(user)
                        .onboardingStep(request.onboardingStep())
                        .build());
        if (profile.isOnboardingCompleted()) {
            throw new GeneralException(ErrorStatus.PROFILE_ALREADY_COMPLETED);
        }

        List<UserJobCategory> currentSelections = findJobCategories(userId);
        List<String> techStackNames = request.techStacks() != null
                ? userTechStackService.replace(user, request.techStacks())
                : null;
        profile.updateDraft(
                request.careerLevel() != null ? request.careerLevel() : profile.getCareerLevel(),
                request.onboardingStep(),
                request.preferenceNotes() != null
                        ? valueCodec.encode(request.preferenceNotes())
                        : profile.getPreferenceNote(),
                request.excludeKeywords() != null
                        ? valueCodec.encode(request.excludeKeywords())
                        : profile.getExcludeKeywords(),
                techStackNames != null
                        ? valueCodec.encode(techStackNames)
                        : profile.getTechStack(),
                request.remoteOk() != null ? request.remoteOk() : profile.isRemoteOk()
        );
        userProfileRepository.save(profile);

        if (request.jobCategoryIds() != null || request.primaryJobCategoryId() != null) {
            List<Long> categoryIds = request.jobCategoryIds() != null
                    ? request.jobCategoryIds()
                    : currentSelections.stream()
                    .map(selection -> selection.getJobCategory().getId())
                    .toList();
            Long primaryId = request.primaryJobCategoryId() != null
                    ? request.primaryJobCategoryId()
                    : currentSelections.stream()
                    .filter(UserJobCategory::isPrimary)
                    .map(selection -> selection.getJobCategory().getId())
                    .findFirst()
                    .orElse(null);
            replaceJobCategories(user, resolveJobCategories(categoryIds, primaryId), primaryId);
        }

        if (request.regionId() != null) {
            replaceRegion(user, resolveRegion(request.regionId()));
        }

        return toDraftResponse(profile, findJobCategories(userId), findRegion(userId));
    }

    @Transactional(readOnly = true)
    public ProfileDraftResponse getDraft(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return null;
        }
        if (profile.isOnboardingCompleted()) {
            throw new GeneralException(ErrorStatus.PROFILE_ALREADY_COMPLETED);
        }
        return toDraftResponse(profile, findJobCategories(userId), findRegion(userId));
    }

    @Transactional
    public void completeOnboarding(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROFILE_NOT_FOUND));
        if (profile.isOnboardingCompleted()) {
            throw new GeneralException(ErrorStatus.PROFILE_ALREADY_COMPLETED);
        }
        if (profile.getOnboardingStep() != OnboardingStep.REVIEW) {
            throw new GeneralException(ErrorStatus.PROFILE_ONBOARDING_NOT_READY);
        }

        List<UserJobCategory> jobCategories = findJobCategories(userId);
        long primaryCount = jobCategories.stream().filter(UserJobCategory::isPrimary).count();
        boolean missingRequiredValue = jobCategories.isEmpty()
                || jobCategories.size() > MAX_JOB_CATEGORIES
                || primaryCount != 1
                || findRegion(userId) == null
                || profile.getCareerLevel() == null;
        if (missingRequiredValue) {
            throw new GeneralException(ErrorStatus.PROFILE_REQUIRED_FIELDS_MISSING);
        }

        profile.completeOnboarding(OffsetDateTime.now());
        eventPublisher.publishEvent(new OnboardingCompletedEvent(userId));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        UserProfile profile = findCompletedProfile(userId);
        return toProfileResponse(profile, findJobCategories(userId), findRegion(userId));
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        if (request == null) {
            UserProfile profile = findCompletedProfile(userId);
            return toProfileResponse(profile, findJobCategories(userId), findRegion(userId));
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        UserProfile profile = findCompletedProfile(userId);
        List<UserJobCategory> currentSelections = findJobCategories(userId);
        List<String> techStackNames = request.techStacks() != null
                ? userTechStackService.replace(user, request.techStacks())
                : null;

        if (request.jobCategoryIds() != null || request.primaryJobCategoryId() != null) {
            List<Long> categoryIds = request.jobCategoryIds() != null
                    ? request.jobCategoryIds()
                    : currentSelections.stream()
                    .map(selection -> selection.getJobCategory().getId())
                    .toList();
            Long primaryId = request.primaryJobCategoryId() != null
                    ? request.primaryJobCategoryId()
                    : currentSelections.stream()
                    .filter(UserJobCategory::isPrimary)
                    .map(selection -> selection.getJobCategory().getId())
                    .findFirst()
                    .orElse(null);
            List<JobCategory> categories = resolveJobCategories(categoryIds, primaryId);
            if (categories.isEmpty()) {
                throw new GeneralException(ErrorStatus.PROFILE_REQUIRED_FIELDS_MISSING);
            }
            replaceJobCategories(user, categories, primaryId);
        }

        if (request.regionId() != null) {
            replaceRegion(user, resolveRegion(request.regionId()));
        }

        profile.updateProfile(
                request.careerLevel() != null ? request.careerLevel() : profile.getCareerLevel(),
                request.preferenceNotes() != null
                        ? valueCodec.encode(request.preferenceNotes())
                        : profile.getPreferenceNote(),
                request.excludeKeywords() != null
                        ? valueCodec.encode(request.excludeKeywords())
                        : profile.getExcludeKeywords(),
                techStackNames != null
                        ? valueCodec.encode(techStackNames)
                        : profile.getTechStack(),
                request.remoteOk() != null ? request.remoteOk() : profile.isRemoteOk()
        );

        List<UserJobCategory> updatedSelections = findJobCategories(userId);
        UserRegion updatedRegion = findRegion(userId);
        if (updatedSelections.isEmpty() || updatedRegion == null || profile.getCareerLevel() == null) {
            throw new GeneralException(ErrorStatus.PROFILE_REQUIRED_FIELDS_MISSING);
        }
        return toProfileResponse(profile, updatedSelections, updatedRegion);
    }

    private List<JobCategory> resolveJobCategories(List<Long> requestedIds, Long primaryId) {
        List<Long> ids = requestedIds == null ? List.of() : requestedIds;
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>(ids);
        if (uniqueIds.size() != ids.size() || uniqueIds.size() > MAX_JOB_CATEGORIES) {
            throw new GeneralException(ErrorStatus.PROFILE_JOB_CATEGORY_COUNT_INVALID);
        }
        if (uniqueIds.isEmpty()) {
            if (primaryId != null) {
                throw new GeneralException(ErrorStatus.PROFILE_PRIMARY_JOB_CATEGORY_INVALID);
            }
            return List.of();
        }
        if (primaryId == null || !uniqueIds.contains(primaryId)) {
            throw new GeneralException(ErrorStatus.PROFILE_PRIMARY_JOB_CATEGORY_INVALID);
        }

        Map<Long, JobCategory> categories = jobCategoryRepository.findAllById(uniqueIds).stream()
                .collect(Collectors.toMap(JobCategory::getId, Function.identity()));
        if (categories.size() != uniqueIds.size()) {
            throw new GeneralException(ErrorStatus.PROFILE_JOB_CATEGORY_NOT_FOUND);
        }
        return uniqueIds.stream().map(categories::get).toList();
    }

    /**
     * 희망 지역은 단일 선택이며 온보딩 완료·프로필 수정에 필요한 필수값이다.
     *
     * <p>요청에 regionId 가 없으면 "변경 없음"으로 보고 기존 선택을 유지한다.
     * JSON 은 필드 누락과 명시적 null 을 구분할 수 없어, null 을 해제로 처리하면
     * 다른 항목만 바꾸는 부분 수정 요청에도 지역이 지워진다. 그래서 해제는 지원하지 않는다.
     */
    private Region resolveRegion(Long requestedId) {
        if (requestedId == null) {
            return null;
        }
        return regionRepository.findById(requestedId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROFILE_REGION_NOT_FOUND));
    }

    private void replaceJobCategories(User user, List<JobCategory> categories, Long primaryId) {
        userJobCategoryRepository.deleteAllByUserId(user.getId());
        userJobCategoryRepository.flush();
        List<UserJobCategory> selections = categories.stream()
                .map(category -> UserJobCategory.builder()
                        .user(user)
                        .jobCategory(category)
                        .primary(category.getId().equals(primaryId))
                        .build())
                .toList();
        userJobCategoryRepository.saveAll(selections);
    }

    private void replaceRegion(User user, Region region) {
        userRegionRepository.deleteAllByUserId(user.getId());
        userRegionRepository.flush();
        if (region == null) {
            return;
        }
        userRegionRepository.save(UserRegion.builder()
                .user(user)
                .region(region)
                .build());
    }

    private List<UserJobCategory> findJobCategories(Long userId) {
        return userJobCategoryRepository.findAllByUserId(userId);
    }

    private UserRegion findRegion(Long userId) {
        return userRegionRepository.findFirstByUserIdOrderByIdDesc(userId).orElse(null);
    }

    private ProfileRegionResponse toRegionResponse(UserRegion userRegion) {
        if (userRegion == null || userRegion.getRegion() == null) {
            return null;
        }
        Region region = userRegion.getRegion();
        return new ProfileRegionResponse(region.getId(), region.getName());
    }

    private ProfileDraftResponse toDraftResponse(
            UserProfile profile,
            List<UserJobCategory> selections,
            UserRegion userRegion
    ) {
        List<UserJobCategory> orderedSelections = new ArrayList<>(selections);
        orderedSelections.sort((left, right) -> {
            if (left.isPrimary() != right.isPrimary()) {
                return left.isPrimary() ? -1 : 1;
            }
            return left.getJobCategory().getId().compareTo(right.getJobCategory().getId());
        });

        List<ProfileJobCategoryResponse> jobCategories = orderedSelections.stream()
                .map(selection -> new ProfileJobCategoryResponse(
                        selection.getJobCategory().getId(),
                        selection.getJobCategory().getName(),
                        selection.isPrimary()
                ))
                .toList();
        return new ProfileDraftResponse(
                profile.getOnboardingStep(),
                jobCategories,
                toRegionResponse(userRegion),
                profile.isRemoteOk(),
                profile.getCareerLevel(),
                valueCodec.decode(profile.getPreferenceNote()),
                valueCodec.decode(profile.getExcludeKeywords()),
                findTechStackNames(profile),
                valueCodec.decode(profile.getPersonalityTags()),
                profile.isJobTestCompleted()
        );
    }

    private UserProfile findCompletedProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROFILE_NOT_FOUND));
        if (!profile.isOnboardingCompleted()) {
            throw new GeneralException(ErrorStatus.PROFILE_NOT_FOUND);
        }
        return profile;
    }

    private ProfileResponse toProfileResponse(
            UserProfile profile,
            List<UserJobCategory> selections,
            UserRegion userRegion
    ) {
        List<ProfileJobCategoryResponse> jobCategories = selections.stream()
                .sorted((left, right) -> {
                    if (left.isPrimary() != right.isPrimary()) {
                        return left.isPrimary() ? -1 : 1;
                    }
                    return left.getJobCategory().getId().compareTo(right.getJobCategory().getId());
                })
                .map(selection -> new ProfileJobCategoryResponse(
                        selection.getJobCategory().getId(),
                        selection.getJobCategory().getName(),
                        selection.isPrimary()
                ))
                .toList();
        return new ProfileResponse(
                profile.getUser().getId(),
                jobCategories,
                toRegionResponse(userRegion),
                profile.isRemoteOk(),
                profile.getCareerLevel(),
                valueCodec.decode(profile.getPreferenceNote()),
                valueCodec.decode(profile.getExcludeKeywords()),
                findTechStackNames(profile),
                valueCodec.decode(profile.getPersonalityTags()),
                profile.isJobTestCompleted(),
                profile.isOnboardingCompleted(),
                profile.getOnboardingCompletedAt()
        );
    }

    private List<String> findTechStackNames(UserProfile profile) {
        List<String> names = userTechStackService.findNames(profile.getUser().getId());
        return names.isEmpty() ? valueCodec.decode(profile.getTechStack()) : names;
    }
}
