package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.AccessTokenConverter;
import com.microtimemanagement.apiservice.converter.RefreshTokenConverter;
import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.factories.*;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.SessionRepository;
import com.microtimemanagement.apiservice.service.impl.SessionServiceImpl;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author vishal.dogra
 * @since 1.0.0
 */

@DisplayName("Session Service Tests")
@ExtendWith(MockitoExtension.class)
public class SessionServiceImplTest {

    @Mock
    JwtUtils jwtUtils;

    @Mock
    SessionRepository sessionRepository;

    RefreshTokenConverter refreshTokenConverter = Mockito.spy(new RefreshTokenConverter(
            Mockito.spy(new AccessTokenConverter())));
    @Spy
    SessionConverter sessionConverter = new SessionConverter(refreshTokenConverter);

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    AccessTokenService accessTokenService;

    @Mock
    RoleService roleService;

    @Mock
    UserService userService;

    @InjectMocks
    SessionServiceImpl sessionService;


    @Test
    @DisplayName("Should create a new session successfully.")
    void shouldCreateNewSession(){
        User user = UserTestFactory.existingAppUserEntity().build();

        SessionPrincipalDTO sessionPrincipalDTO = SessionPrincipalDTO.builder()
                .uid(user.getUid())
                .authorities(user.getRoles())
                .build();
        RefreshToken refreshToken = RefreshTokenTestDataFactory.mockRefreshTokenEntity()
                .accessTokens(List.of(AccessTokenTestDataFactory.newAccessTokenEntity().build()))
                .build();

        Mockito.when(sessionRepository.findByUserAndIsActiveTrue(user)).thenReturn(Optional.empty());
        Mockito.when(refreshTokenService.createRefreshToken(sessionPrincipalDTO)).thenReturn(refreshToken);
        Mockito.when(sessionRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        SessionDTO sessionDTO = sessionService.createNewSession(user);

        assertThat(sessionDTO.getUser()).isEqualTo(user);
        assertThat(sessionDTO.getRefreshTokenDTO()).isEqualTo(refreshTokenConverter.toDTO(refreshToken));


    }

    @Test
    @DisplayName("Should destroy existing session correctly by setting isActive to false for session->refreshToken->accessTokens.")
    void shouldDestroyExistingSession(){
        Session session = SessionTestDataFactory.mockSessionEntity().build();

        RefreshToken refreshToken = RefreshTokenTestDataFactory
                .mockRefreshTokenEntity()
                .session(session)
                .build();

        AccessToken accessToken = AccessTokenTestDataFactory
                .newAccessTokenEntity()
                .refreshToken(refreshToken)
                .build();

        Mockito.when(accessTokenService.findByToken(Mockito.anyString())).thenReturn(accessToken);
        Mockito.when(sessionRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());
        Mockito.when(refreshTokenService.saveRefreshToken(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());
        Mockito.when(accessTokenService.saveAccessTokens(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        sessionService.destroySession(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN);

        assertThat(accessToken.getIsActive()).isFalse();
        assertThat(refreshToken.getIsActive()).isFalse();
        assertThat(session.getIsActive()).isFalse();
    }


    @Test
    @DisplayName("Should refresh the session successfully for valid refresh token value by setting previous access token invalid and issuing a fresh access token.")
    void shouldRefreshSession(){

        String previousAccessTokenId="Previous Active AccessToken";

        User user = UserTestFactory.existingAppUserEntity()
                .roles(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES)
                .build();

        Session session = SessionTestDataFactory
                .mockSessionEntity()
                .user(user)
                .build();

        List<AccessToken> accessTokens = new ArrayList<>();
        accessTokens.add(AccessTokenTestDataFactory
                .newAccessTokenEntity()
                        .id(previousAccessTokenId)
                .build());

        RefreshToken refreshToken = RefreshTokenTestDataFactory
                .mockRefreshTokenEntity()
                .expiresAt(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(5)))
                .session(session)
                .accessTokens(accessTokens)
                .build();

        session.setRefreshToken(refreshToken);

        AccessToken accessToken = AccessTokenTestDataFactory.newAccessTokenEntity().build();

        Mockito.when(refreshTokenService.findEntityByActiveToken(Mockito.anyString())).thenReturn(refreshToken);
        Mockito.when(roleService.getRoleNamesForIds(Mockito.any())).thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        Mockito.when(accessTokenService.createAccessToken(Mockito.any())).thenReturn(accessToken);
        Mockito.when(accessTokenService.saveAccessTokens(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());
        Mockito.when(refreshTokenService.saveRefreshToken(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        SessionDTO sessionDTO = sessionService.refreshSession(AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN);

        assertThat(sessionDTO.getRefreshTokenDTO().getToken())
                .isEqualTo(refreshToken.getToken());

        assertThat(sessionDTO.getRefreshTokenDTO().getActiveAccessTokenDTO().getId())
                .isNotEqualTo(previousAccessTokenId);

        assertThat(sessionDTO.getRefreshTokenDTO()
                .getAccessTokenDTOList().stream()
                .filter(accessTokenDTO -> accessTokenDTO.getId().equals(previousAccessTokenId))
                .toList().get(0).getId()
        ).isEqualTo(previousAccessTokenId);

    }

    @Test
    @DisplayName("Session should be invalid when no session is found for the given refresh token.")
    void shouldThrowNotFoundException_withSessionInvalidMessage_onRefreshSession(){
        Mockito.when(refreshTokenService.findEntityByActiveToken(Mockito.anyString())).thenReturn(null);

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> {
                    sessionService.refreshSession(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN);
                }).withMessage(ErrorConstants.SESSION_TOKEN_INVALID);
    }

    @Test
    @DisplayName("Should throw Not Found Exception with Session Expired message if the refresh token is expired.")
    void shouldThrowNotFound_withSessionExpiredMessage_onRefreshSession_whenExpiredRefreshToken(){
        RefreshToken refreshToken = RefreshTokenTestDataFactory
                .mockRefreshTokenEntity()
                .expiresAt(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5)))
                .build();

        Mockito.when(refreshTokenService.findEntityByActiveToken(Mockito.anyString())).thenReturn(refreshToken);

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class).isThrownBy(() -> {
            sessionService.refreshSession(AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN);
        }).withMessage(ErrorConstants.SESSION_EXPIRED);

    }

    @Test
    @DisplayName("Should throw Not Found Exception with Session Expired message if the associated session is not active.")
    void shouldThrowNotFound_withSessionExpiredMessage_onRefreshSession_whenInactiveSessionEntity(){
        RefreshToken refreshToken = RefreshTokenTestDataFactory
                .mockRefreshTokenEntity()
                .expiresAt(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(5)))
                .session(SessionTestDataFactory
                        .mockSessionEntity()
                        .isActive(Boolean.FALSE)
                        .build())
                .build();

        Mockito.when(refreshTokenService.findEntityByActiveToken(Mockito.anyString())).thenReturn(refreshToken);

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class).isThrownBy(() -> {
            sessionService.refreshSession(AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN);
        }).withMessage(ErrorConstants.SESSION_EXPIRED);

    }

    @Test
    void shouldValidateCurrentUserSessionForAccessToken(){
        User user = UserTestFactory.existingAppUserEntity().build();
        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .token(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN).build();

        Mockito.when(jwtUtils.isTokenExpired(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(false);
        Mockito.when(jwtUtils.extractPrincipalFromToken(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(user.getUid());
        Mockito.when(userService.getUserByUid(user.getUid())).thenReturn(user);
        Mockito.when(jwtUtils.isValidTokenSubject(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN, user)).thenReturn(true);
        Mockito.when(accessTokenService.findAccessToken(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(accessTokenDTO);
        Mockito.when(roleService.getRoleNamesForIds(user.getRoles())).thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);

        ValidSessionDTO validSessionDTO = sessionService.validateSessionForAccessToken(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN);

        Assertions.assertThat(validSessionDTO.getIsValidSession()).isEqualTo(Boolean.TRUE);
        Assertions.assertThat(validSessionDTO.getPrincipal()).isEqualTo(user);
        Assertions.assertThat(validSessionDTO.getError()).isNull();

    }

    @Test
    void shouldReturnSessionExpiredError_onValidateCurrentUserSessionForAccessToken(){
        Mockito.when(jwtUtils.isTokenExpired(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(true);

        ValidSessionDTO validSessionDTO = sessionService.validateSessionForAccessToken(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN);

        Assertions.assertThat(validSessionDTO.getIsValidSession()).isEqualTo(Boolean.FALSE);
        Assertions.assertThat(validSessionDTO.getPrincipal()).isNull();
        Assertions.assertThat(validSessionDTO.getError()).isNotNull();
        Assertions.assertThat(validSessionDTO.getError()).isEqualTo(ErrorConstants.SESSION_EXPIRED);

    }

    @Test
    void shouldReturnInvalidSessionError_onValidateCurrentUserSessionForAccessToken(){
        Mockito.when(jwtUtils.isTokenExpired(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(false);
        Mockito.when(jwtUtils.extractPrincipalFromToken(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(null);

        ValidSessionDTO validSessionDTO = sessionService.validateSessionForAccessToken(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN);

        Assertions.assertThat(validSessionDTO.getIsValidSession()).isEqualTo(Boolean.FALSE);
        Assertions.assertThat(validSessionDTO.getPrincipal()).isNull();
        Assertions.assertThat(validSessionDTO.getError()).isNotNull();
        Assertions.assertThat(validSessionDTO.getError()).isEqualTo(ErrorConstants.SESSION_TOKEN_INVALID);

    }




}
