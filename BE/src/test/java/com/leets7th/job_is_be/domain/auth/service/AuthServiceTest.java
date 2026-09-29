package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RefreshTokenSessionStore sessionStore;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserWithdrawalRepository userWithdrawalRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                tokenProvider,
                sessionStore,
                userRepository,
                userProfileRepository,
                userWithdrawalRepository
        );
    }

    @Test
    void rotatesRefreshTokenWhenReissuing() {
        String oldRefreshToken = "old-refresh-token";
        JwtTokenProvider.RefreshTokenClaims claims =
                new JwtTokenProvider.RefreshTokenClaims(1L, "old-session");
        JwtTokenProvider.TokenPair newPair = new JwtTokenProvider.TokenPair(
                "new-access-token",
                "new-refresh-token",
                "new-session",
                1800,
                Duration.ofDays(14)
        );

        when(tokenProvider.decodeRefreshToken(oldRefreshToken)).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(tokenProvider.issueTokenPair(1L, "USER")).thenReturn(newPair);
        when(sessionStore.rotate(
                "old-session",
                1L,
                oldRefreshToken,
                "new-session",
                "new-refresh-token",
                Duration.ofDays(14)
        )).thenReturn(true);

        AuthService.ReissueResult result = authService.reissue(oldRefreshToken);

        assertEquals("new-access-token", result.response().accessToken());
        assertEquals(1800, result.response().expiresIn());
        assertEquals("new-refresh-token", result.refreshToken());
        verify(sessionStore).rotate(
                "old-session",
                1L,
                oldRefreshToken,
                "new-session",
                "new-refresh-token",
                Duration.ofDays(14)
        );
    }

    @Test
    void rejectsAlreadyConsumedRefreshToken() {
        String refreshToken = "refresh-token";
        when(tokenProvider.decodeRefreshToken(refreshToken))
                .thenReturn(new JwtTokenProvider.RefreshTokenClaims(1L, "session"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        JwtTokenProvider.TokenPair newPair = new JwtTokenProvider.TokenPair(
                "new-access-token",
                "new-refresh-token",
                "new-session",
                1800,
                Duration.ofDays(14)
        );
        when(tokenProvider.issueTokenPair(1L, "USER")).thenReturn(newPair);
        when(sessionStore.rotate(
                "session",
                1L,
                refreshToken,
                "new-session",
                "new-refresh-token",
                Duration.ofDays(14)
        )).thenReturn(false);

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> authService.reissue(refreshToken)
        );

        assertEquals(ErrorStatus.REFRESH_SESSION_NOT_FOUND, exception.getErrorStatus());
    }

    @Test
    void keepsRefreshSessionWhenUserLookupFails() {
        String refreshToken = "refresh-token";
        when(tokenProvider.decodeRefreshToken(refreshToken))
                .thenReturn(new JwtTokenProvider.RefreshTokenClaims(1L, "session"));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> authService.reissue(refreshToken)
        );

        assertEquals(ErrorStatus.USER_NOT_FOUND, exception.getErrorStatus());
        verify(sessionStore, never()).rotate(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void logoutWithoutCookieIsIdempotent() {
        authService.logout(null);
    }

    @Test
    void rejectsTokenReissueForWithdrawnAccount() {
        String refreshToken = "refresh-token";
        User user = user(1L);
        user.withdraw(OffsetDateTime.now());
        when(tokenProvider.decodeRefreshToken(refreshToken))
                .thenReturn(new JwtTokenProvider.RefreshTokenClaims(1L, "session"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> authService.reissue(refreshToken)
        );

        assertEquals(ErrorStatus.WITHDRAWN_ACCOUNT_TOKEN_REISSUE, exception.getErrorStatus());
        verify(sessionStore, never()).rotate(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    private User user(Long id) {
        User user = User.builder()
                .socialId("social-id")
                .socialType(SocialType.KAKAO)
                .email("user@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
