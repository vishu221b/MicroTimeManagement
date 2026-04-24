package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.AccessTokenConverter;
import com.microtimemanagement.apiservice.converter.RefreshTokenConverter;
import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
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

import static org.assertj.core.api.Assertions.*;

/**
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

    @Mock
    AccessTokenService accessTokenService;

    @Mock
    RoleService roleService;

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
    @DisplayName("Should fetch session by refresh token value.")
    void shouldGetSessionByToken(){}

    @Test
    @DisplayName("Should refresh session successfully for valid refresh token value.")
    void shouldRefreshSession(){

    }

    @Test
    @DisplayName("Session should be invalid on session not found for the given refresh token value.")
    void shouldThrowNotFoundException_withSessionInvalidMessage_onRefreshSession(){
        Mockito.when(refreshTokenService.findEntityByActiveToken(Mockito.anyString())).thenReturn(null);

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> {
                    sessionService.refreshSession(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN);
                }).withMessage(ErrorConstants.SESSION_TOKEN_INVALID);
    }


}
