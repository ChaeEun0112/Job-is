package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.dto.OAuthExchangeResponse;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthClientRegistry;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthLoginCodeStore;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthRestoreCodeStore;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthStateStore;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthUserInfo;
import com.leets7th.job_is_be.domain.auth.oauth.SocialOAuthClient;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserWithdrawal;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
public class OAuthLoginService {

    private final OAuthClientRegistry clientRegistry;
    private final OAuthStateStore stateStore;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenSessionStore refreshTokenSessionStore;
    private final OAuthLoginCodeStore loginCodeStore;
    private final OAuthRestoreCodeStore restoreCodeStore;

    public OAuthLoginService(
            OAuthClientRegistry clientRegistry,
            OAuthStateStore stateStore,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserWithdrawalRepository userWithdrawalRepository,
            JwtTokenProvider tokenProvider,
            RefreshTokenSessionStore refreshTokenSessionStore,
            OAuthLoginCodeStore loginCodeStore,
            OAuthRestoreCodeStore restoreCodeStore
    ) {
        this.clientRegistry = clientRegistry;
        this.stateStore = stateStore;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userWithdrawalRepository = userWithdrawalRepository;
        this.tokenProvider = tokenProvider;
        this.refreshTokenSessionStore = refreshTokenSessionStore;
        this.loginCodeStore = loginCodeStore;
        this.restoreCodeStore = restoreCodeStore;
    }

    public AuthorizationRequest createAuthorizationRequest(String provider) {
        SocialType socialType = clientRegistry.parseProvider(provider);
        SocialOAuthClient client = clientRegistry.get(socialType);
        String state = stateStore.create(socialType);
        return new AuthorizationRequest(client.buildAuthorizationUri(state), state);
    }

    @Transactional
    public OAuthLoginResult login(String provider, String code, String state, String cookieState) {
        SocialType socialType = clientRegistry.parseProvider(provider);
        if (code == null || code.isBlank()) {
            throw new GeneralException(ErrorStatus.OAUTH_CODE_MISSING);
        }
        if (!stateStore.consume(state, cookieState, socialType)) {
            throw new GeneralException(ErrorStatus.OAUTH_STATE_INVALID);
        }

        OAuthUserInfo userInfo = clientRegistry.get(socialType).getUserInfo(code);
        validateUserInfo(userInfo);
        LoginUser loginUser = findOrCreateUser(userInfo);
        Long userId = loginUser.user().getId();
        if (loginUser.restorableUntil() != null) {
            String restoreCode = restoreCodeStore.create(userId, loginUser.restorableUntil());
            return OAuthLoginResult.restoration(restoreCode, loginUser.restorableUntil());
        }
        JwtTokenProvider.TokenPair tokenPair = tokenProvider.issueTokenPair(userId, loginUser.user().getRole().name());
        boolean onboardingCompleted = userProfileRepository.findByUserId(userId)
                .map(profile -> profile.isOnboardingCompleted())
                .orElse(false);
        String loginCode = loginCodeStore.create(
                new OAuthLoginCodeStore.LoginPayload(
                        userId,
                        loginUser.newUser(),
                        onboardingCompleted,
                        tokenPair.accessToken(),
                        tokenPair.refreshToken(),
                        tokenPair.refreshSessionId(),
                        tokenPair.accessTokenExpiresIn(),
                        tokenPair.refreshTokenTtl().toSeconds()
                )
        );

        return OAuthLoginResult.login(loginCode);
    }

    public ExchangeResult exchange(String loginCode) {
        if (loginCode == null || loginCode.isBlank()) {
            throw new GeneralException(ErrorStatus.OAUTH_LOGIN_CODE_MISSING);
        }

        OAuthLoginCodeStore.LoginPayload payload = loginCodeStore.consume(loginCode)
                .orElseThrow(() -> new GeneralException(ErrorStatus.OAUTH_LOGIN_CODE_INVALID));
        refreshTokenSessionStore.save(
                payload.refreshSessionId(),
                payload.userId(),
                payload.refreshToken(),
                payload.refreshTokenTtl()
        );

        return new ExchangeResult(
                new OAuthExchangeResponse(
                        payload.accessToken(),
                        payload.userId(),
                        payload.newUser(),
                        payload.onboardingCompleted()
                ),
                payload.refreshToken()
        );
    }

    private void validateUserInfo(OAuthUserInfo userInfo) {
        if (userInfo == null
                || userInfo.socialId() == null
                || userInfo.socialId().isBlank()
                || userInfo.socialType() == null
                || userInfo.email() == null
                || userInfo.email().isBlank()) {
            throw new GeneralException(ErrorStatus.OAUTH_USER_INFO_INVALID);
        }
    }

    private LoginUser findOrCreateUser(OAuthUserInfo userInfo) {
        return userRepository
                .findBySocialIdAndSocialType(userInfo.socialId(), userInfo.socialType())
                .map(user -> existingUser(user))
                .orElseGet(() -> createUser(userInfo));
    }

    private LoginUser createUser(OAuthUserInfo userInfo) {
        String normalizedEmail = userInfo.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new GeneralException(ErrorStatus.SOCIAL_ACCOUNT_CONFLICT);
        }

        User user = User.builder()
                .socialId(userInfo.socialId())
                .socialType(userInfo.socialType())
                .email(normalizedEmail)
                .build();
        return new LoginUser(userRepository.save(user), true, null);
    }

    private LoginUser existingUser(User user) {
        if (user.getStatus() != UserStatus.WITHDRAWN) {
            return new LoginUser(user, false, null);
        }

        UserWithdrawal withdrawal = userWithdrawalRepository
                .findFirstByUserIdAndStatusOrderByRequestedAtDesc(user.getId(), WithdrawalStatus.PENDING)
                .orElseThrow(() -> new GeneralException(ErrorStatus.WITHDRAWAL_RESTORE_EXPIRED));
        OffsetDateTime now = OffsetDateTime.now();
        if (withdrawal.getScheduledDeletionAt() == null
                || !now.isBefore(withdrawal.getScheduledDeletionAt())) {
            throw new GeneralException(ErrorStatus.WITHDRAWAL_RESTORE_EXPIRED);
        }

        return new LoginUser(user, false, withdrawal.getScheduledDeletionAt());
    }

    private record LoginUser(
            User user,
            boolean newUser,
            OffsetDateTime restorableUntil
    ) {
    }

    public record OAuthLoginResult(
            String loginCode,
            String restoreCode,
            OffsetDateTime restorableUntil
    ) {
        public static OAuthLoginResult login(String loginCode) {
            return new OAuthLoginResult(loginCode, null, null);
        }

        public static OAuthLoginResult restoration(
                String restoreCode,
                OffsetDateTime restorableUntil
        ) {
            return new OAuthLoginResult(null, restoreCode, restorableUntil);
        }

        public boolean restorationRequired() {
            return restoreCode != null;
        }
    }

    public record ExchangeResult(OAuthExchangeResponse response, String refreshToken) {
    }

    public record AuthorizationRequest(URI uri, String state) {
    }
}
