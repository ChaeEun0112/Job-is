package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.DevLoginResponse;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Service
public class DevAuthService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenSessionStore sessionStore;
    private final UserRepository userRepository;

    public DevAuthService(
            JwtTokenProvider tokenProvider,
            RefreshTokenSessionStore sessionStore,
            UserRepository userRepository
    ) {
        this.tokenProvider = tokenProvider;
        this.sessionStore = sessionStore;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DevLoginResponse login(Long userId) {
        if (userId == null) {
            throw new GeneralException(ErrorStatus.DEV_LOGIN_USER_ID_REQUIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        JwtTokenProvider.TokenPair tokenPair = tokenProvider.issueTokenPair(userId, user.getRole().name());
        sessionStore.save(
                tokenPair.refreshSessionId(),
                userId,
                tokenPair.refreshToken(),
                tokenPair.refreshTokenTtl()
        );

        return new DevLoginResponse(
                userId,
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                "Bearer",
                tokenPair.accessTokenExpiresIn()
        );
    }
}
