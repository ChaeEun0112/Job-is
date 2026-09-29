package com.leets7th.job_is_be.domain.user.service;

import com.leets7th.job_is_be.domain.job.entity.TechStack;
import com.leets7th.job_is_be.domain.job.service.TechStackRegistrationService;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserTechStack;
import com.leets7th.job_is_be.domain.user.repository.UserTechStackRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class UserTechStackService {

    private static final int MAX_NAME_LENGTH = 100;

    private final TechStackRegistrationService techStackRegistrationService;
    private final UserTechStackRepository userTechStackRepository;

    public UserTechStackService(
            TechStackRegistrationService techStackRegistrationService,
            UserTechStackRepository userTechStackRepository
    ) {
        this.techStackRegistrationService = techStackRegistrationService;
        this.userTechStackRepository = userTechStackRepository;
    }

    public List<String> replace(User user, List<String> requestedNames) {
        Map<String, String> uniqueNames = normalize(requestedNames);
        List<TechStack> techStacks = uniqueNames.entrySet().stream()
                .map(entry -> techStackRegistrationService.resolve(
                        entry.getValue(),
                        entry.getKey()
                ))
                .toList();

        userTechStackRepository.deleteAllByUserId(user.getId());
        userTechStackRepository.flush();
        userTechStackRepository.saveAll(techStacks.stream()
                .map(techStack -> UserTechStack.builder()
                        .user(user)
                        .techStack(techStack)
                        .build())
                .toList());

        return techStacks.stream().map(TechStack::getName).toList();
    }

    public List<String> findNames(Long userId) {
        return userTechStackRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .map(userTechStack -> userTechStack.getTechStack().getName())
                .toList();
    }

    private Map<String, String> normalize(List<String> requestedNames) {
        Map<String, String> uniqueNames = new LinkedHashMap<>();
        for (String requestedName : requestedNames) {
            if (requestedName == null || requestedName.isBlank()) {
                throw new GeneralException(ErrorStatus.PROFILE_TECH_STACK_INVALID);
            }
            String displayName = requestedName.trim().replaceAll("\\s+", " ");
            if (displayName.length() > MAX_NAME_LENGTH) {
                throw new GeneralException(ErrorStatus.PROFILE_TECH_STACK_INVALID);
            }
            uniqueNames.putIfAbsent(displayName.toLowerCase(Locale.ROOT), displayName);
        }
        return uniqueNames;
    }
}
