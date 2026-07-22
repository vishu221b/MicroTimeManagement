package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.AccessTokenConverter;
import com.microtimemanagement.apiservice.converter.RefreshTokenConverter;
import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionTokenDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.factories.*;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.SessionRepository;
import com.microtimemanagement.apiservice.service.impl.SessionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Unit tests for {@link SessionServiceImpl}.
 *
 * <p>The service was refactored to delegate token/session bookkeeping to
 * {@link RefreshTokenService}; these tests exercise the thin orchestration that
 * remains here (create / revoke-on-recreate / destroy / delegate-validate /
 * refresh + inactive-session guard). The deep token-validation scenarios now
 * live behind {@link RefreshTokenService} and are covered there.
 *
 * @author vishal.dogra
 * @since 1.0.0
 */

@DisplayName("Session Service Tests")
@ExtendWith(MockitoExtension.class)
public class SessionServiceImplTest {

    @Mock
    SessionRepository sessionRepository;

    RefreshTokenConverter refreshTokenConverter = Mockito.spy(new RefreshTokenConverter(
            Mockito.spy(new AccessTokenConverter())));

    @Spy
    SessionConverter sessionConverter = new SessionConverter(refreshTokenConverter);

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    SessionServiceImpl sessionService;


    @Test
    @DisplayName("Should create a new session successfully when the user has no active session.")
    void shouldCreateNewSession() {
        User user = UserTestFactory.existingAppUserEntity().build();

        RefreshToken refreshToken = RefreshTokenTestDataFactory.mockRefreshTokenEntity()
                .accessTokens(List.of(AccessTokenTestDataFactory.newAccessTokenEntity().build()))
                .build();

        Mockito.when(sessionRepository.findByUserAndIsActiveTrue(user)).thenReturn(Optional.empty());
        Mockito.when(refreshTokenService.createRefreshToken(Mockito.any())).thenReturn(refreshToken);
        Mockito.when(sessionRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        SessionDTO sessionDTO = sessionService.createNewSession(user);

        assertThat(sessionDTO.getUser()).isEqualTo(user);
        assertThat(sessionDTO.getRefreshTokenDTO()).isEqualTo(refreshTokenConverter.toDTO(refreshToken));
    }

    @Test
    @DisplayName("Should revoke an existing active session before minting a new one (single-session-per-user).")
    void shouldRevokeExistingActiveSession_beforeCreatingNew() {
        User user = UserTestFactory.existingAppUserEntity().build();

        RefreshToken existingRefreshToken = RefreshTokenTestDataFactory.mockRefreshTokenEntity().build();
        Session existingSession = SessionTestDataFactory.mockSessionEntity()
                .user(user)
                .refreshToken(existingRefreshToken)
                .build();

        RefreshToken freshRefreshToken = RefreshTokenTestDataFactory.mockRefreshTokenEntity()
                .accessTokens(List.of(AccessTokenTestDataFactory.newAccessTokenEntity().build()))
                .build();

        Mockito.when(sessionRepository.findByUserAndIsActiveTrue(user)).thenReturn(Optional.of(existingSession));
        Mockito.when(refreshTokenService.createRefreshToken(Mockito.any())).thenReturn(freshRefreshToken);
        Mockito.when(sessionRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        sessionService.createNewSession(user);

        assertThat(existingSession.getIsActive()).isFalse();
        Mockito.verify(refreshTokenService).revokeRefreshToken(existingRefreshToken);
    }

    @Test
    @DisplayName("Should destroy the session backing a given access token by marking it inactive.")
    void shouldDestroySessionForAccessToken() {
        Session session = SessionTestDataFactory.mockSessionEntity().build();
        RefreshToken refreshToken = RefreshTokenTestDataFactory.mockRefreshTokenEntity()
                .session(session)
                .build();

        Mockito.when(refreshTokenService.revokeRefreshTokenUsingAccessToken(Mockito.anyString()))
                .thenReturn(refreshToken);
        Mockito.when(sessionRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        sessionService.destroySessionForAccessToken(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN);

        assertThat(session.getIsActive()).isFalse();
        Mockito.verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should not touch any token/session when the supplied access token is blank.")
    void shouldSkipDestroy_whenTokenBlank() {
        sessionService.destroySessionForAccessToken("   ");

        Mockito.verifyNoInteractions(refreshTokenService);
        Mockito.verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Should delegate access-token validation to the refresh token service.")
    void shouldDelegateValidateSessionForAccessToken() {
        ValidSessionTokenDTO expected = ValidSessionTokenDTO.builder()
                .isValidSession(Boolean.TRUE)
                .build();

        Mockito.when(refreshTokenService.validateAccessToken(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN))
                .thenReturn(expected);

        ValidSessionTokenDTO actual = sessionService.validateSessionForAccessToken(
                AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("Should refresh the session for a valid, active refresh token.")
    void shouldRefreshSession() {
        User user = UserTestFactory.existingAppUserEntity().build();

        AccessToken accessToken = AccessTokenTestDataFactory.newAccessTokenEntity().build();
        Session session = SessionTestDataFactory.mockSessionEntity().user(user).build();
        RefreshToken refreshToken = RefreshTokenTestDataFactory.mockRefreshTokenEntity()
                .session(session)
                .accessTokens(List.of(accessToken))
                .build();
        session.setRefreshToken(refreshToken);

        Mockito.when(refreshTokenService.validateRefreshToken(Mockito.anyString())).thenReturn(refreshToken);
        Mockito.when(refreshTokenService.refreshSessionForRefreshToken(refreshToken)).thenReturn(refreshToken);

        SessionDTO sessionDTO = sessionService.refreshSession(
                AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN);

        assertThat(sessionDTO.getRefreshTokenDTO().getToken()).isEqualTo(refreshToken.getToken());
        assertThat(sessionDTO.getUser()).isEqualTo(user);
        Mockito.verify(refreshTokenService).refreshSessionForRefreshToken(refreshToken);
    }

    @Test
    @DisplayName("Should throw Not Found (Session Expired) when the associated session is inactive.")
    void shouldThrowSessionExpired_whenAssociatedSessionInactive() {
        Session inactiveSession = SessionTestDataFactory.mockSessionEntity()
                .isActive(Boolean.FALSE)
                .build();
        RefreshToken refreshToken = RefreshTokenTestDataFactory.mockRefreshTokenEntity()
                .session(inactiveSession)
                .build();

        Mockito.when(refreshTokenService.validateRefreshToken(Mockito.anyString())).thenReturn(refreshToken);

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> sessionService.refreshSession(
                        AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN))
                .withMessage(ErrorConstants.SESSION_EXPIRED);
    }

    @Test
    @DisplayName("Should propagate the Not Found error raised by refresh-token validation.")
    void shouldPropagateNotFound_whenRefreshTokenInvalid() {
        Mockito.when(refreshTokenService.validateRefreshToken(Mockito.anyString()))
                .thenThrow(new MicroTimeManagementNotFoundException(ErrorConstants.SESSION_TOKEN_INVALID));

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> sessionService.refreshSession(
                        AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN))
                .withMessage(ErrorConstants.SESSION_TOKEN_INVALID);
    }
}
