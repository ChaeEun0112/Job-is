package com.leets7th.job_is_be.domain.auth.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthLoginServiceTest {

    @Mock private OAuthClientRegistry clientRegistry;
    @Mock private OAuthStateStore stateStore;
    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private UserWithdrawalRepository userWithdrawalRepository;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private RefreshTokenSessionStore sessionStore;
    @Mock private OAuthLoginCodeStore loginCodeStore;
    @Mock private OAuthRestoreCodeStore restoreCodeStore;
    @Mock private SocialOAuthClient oauthClient;

    private OAuthLoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new OAuthLoginService(
                clientRegistry,
                stateStore,
                userRepository,
                userProfileRepository,
                userWithdrawalRepository,
                tokenProvider,
                sessionStore,
                loginCodeStore,
                restoreCodeStore
        );
    }

    @Test
    void createsNewUserAndLoginCode() {
        OAuthUserInfo userInfo = new OAuthUserInfo("social-id", SocialType.GOOGLE, "USER@EXAMPLE.COM");
        User savedUser = user("social-id", SocialType.GOOGLE, "user@example.com", 1L);
        prepareOAuth(userInfo);
        when(userRepository.findBySocialIdAndSocialType("social-id", SocialType.GOOGLE))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.issueTokenPair(1L, "USER")).thenReturn(tokenPair());
        when(loginCodeStore.create(any(OAuthLoginCodeStore.LoginPayload.class)))
                .thenReturn("login-code");

        OAuthLoginService.OAuthLoginResult result = loginService.login("google", "code", "state", "state");

        assertEquals("login-code", result.loginCode());
        verify(loginCodeStore).create(any(OAuthLoginCodeStore.LoginPayload.class));
        verify(sessionStore, never()).save(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void rejectsEmailRegisteredWithAnotherSocialAccount() {
        OAuthUserInfo userInfo = new OAuthUserInfo("new-social-id", SocialType.GOOGLE, "user@example.com");
        prepareOAuth(userInfo);
        when(userRepository.findBySocialIdAndSocialType("new-social-id", SocialType.GOOGLE))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user("kakao-id", SocialType.KAKAO, "user@example.com", 1L)));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> loginService.login("google", "code", "state", "state")
        );

        assertEquals(ErrorStatus.SOCIAL_ACCOUNT_CONFLICT, exception.getErrorStatus());
    }

    @Test
    void issuesRestoreCodeWithoutRestoringWithdrawnUser() {
        User user = user("social-id", SocialType.KAKAO, "user@example.com", 1L);
        user.withdraw(OffsetDateTime.now().minusDays(1));
        UserWithdrawal withdrawal = UserWithdrawal.builder()
                .user(user)
                .requestedAt(OffsetDateTime.now().minusDays(1))
                .scheduledDeletionAt(OffsetDateTime.now().plusDays(29))
                .build();
        prepareOAuth(new OAuthUserInfo("social-id", SocialType.KAKAO, "user@example.com"));
        when(userRepository.findBySocialIdAndSocialType("social-id", SocialType.KAKAO))
                .thenReturn(Optional.of(user));
        when(userWithdrawalRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(
                1L, WithdrawalStatus.PENDING)).thenReturn(Optional.of(withdrawal));
        when(restoreCodeStore.create(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(OffsetDateTime.class)
        )).thenReturn("restore-code");

        OAuthLoginService.OAuthLoginResult result = loginService.login("kakao", "code", "state", "state");

        assertEquals("restore-code", result.restoreCode());
        assertTrue(result.restorationRequired());
        assertEquals(UserStatus.WITHDRAWN, user.getStatus());
        assertEquals(WithdrawalStatus.PENDING, withdrawal.getStatus());
        verify(tokenProvider, never()).issueTokenPair(any(), any());
        verify(loginCodeStore, never()).create(any());
    }

    @Test
    void exchangesLoginCodeForAccessTokenAndRefreshSession() {
        OAuthLoginCodeStore.LoginPayload payload = new OAuthLoginCodeStore.LoginPayload(
                1L,
                true,
                false,
                "access",
                "refresh",
                "session",
                1800,
                Duration.ofDays(14).toSeconds()
        );
        when(loginCodeStore.consume("login-code")).thenReturn(Optional.of(payload));

        OAuthLoginService.ExchangeResult result = loginService.exchange("login-code");

        assertEquals("access", result.response().accessToken());
        assertEquals(1L, result.response().userId());
        assertTrue(result.response().isNewUser());
        assertFalse(result.response().onboardingCompleted());
        assertEquals("refresh", result.refreshToken());
        verify(sessionStore).save("session", 1L, "refresh", Duration.ofDays(14));
    }

    @Test
    void rejectsExpiredOrConsumedLoginCode() {
        when(loginCodeStore.consume("expired-code")).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> loginService.exchange("expired-code")
        );

        assertEquals(ErrorStatus.OAUTH_LOGIN_CODE_INVALID, exception.getErrorStatus());
    }

    @Test
    void rejectsInvalidStateBeforeCallingProvider() {
        when(clientRegistry.parseProvider("google")).thenReturn(SocialType.GOOGLE);
        when(stateStore.consume("invalid", "invalid", SocialType.GOOGLE)).thenReturn(false);

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> loginService.login("google", "code", "invalid", "invalid")
        );

        assertEquals(ErrorStatus.OAUTH_STATE_INVALID, exception.getErrorStatus());
        verify(clientRegistry, never()).get(SocialType.GOOGLE);
    }

    @Test
    void rejectsProviderUserInfoWithoutEmail() {
        prepareOAuth(new OAuthUserInfo("social-id", SocialType.GOOGLE, " "));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> loginService.login("google", "code", "state", "state")
        );

        assertEquals(ErrorStatus.OAUTH_USER_INFO_INVALID, exception.getErrorStatus());
        verify(userRepository, never()).findBySocialIdAndSocialType(any(), any());
    }

    @Test
    void rejectsProviderUserInfoWithoutSocialId() {
        prepareOAuth(new OAuthUserInfo(" ", SocialType.GOOGLE, "user@example.com"));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> loginService.login("google", "code", "state", "state")
        );

        assertEquals(ErrorStatus.OAUTH_USER_INFO_INVALID, exception.getErrorStatus());
        verify(userRepository, never()).findBySocialIdAndSocialType(any(), any());
    }

    private void prepareOAuth(OAuthUserInfo userInfo) {
        when(clientRegistry.parseProvider(userInfo.socialType().name().toLowerCase()))
                .thenReturn(userInfo.socialType());
        when(stateStore.consume("state", "state", userInfo.socialType())).thenReturn(true);
        when(clientRegistry.get(userInfo.socialType())).thenReturn(oauthClient);
        when(oauthClient.getUserInfo("code")).thenReturn(userInfo);
    }

    private User user(String socialId, SocialType socialType, String email, Long id) {
        User user = User.builder()
                .socialId(socialId)
                .socialType(socialType)
                .email(email)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private JwtTokenProvider.TokenPair tokenPair() {
        return new JwtTokenProvider.TokenPair(
                "access", "refresh", "session", 1800, Duration.ofDays(14)
        );
    }
}
