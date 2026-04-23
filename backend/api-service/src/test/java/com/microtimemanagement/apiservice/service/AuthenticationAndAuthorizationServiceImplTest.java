package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.RefreshTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.dto.request.AccessTokenRefreshRequestDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.factories.AuthTestDataFactory;
import com.microtimemanagement.apiservice.factories.UserTestFactory;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.service.impl.AuthenticationAndAuthorizationServiceImpl;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AuthenticationAndAuthorizationServiceImplTest {

    @Mock
    JwtUtils jwtUtils;

    @Mock
    UserService userService;

    @Mock
    SessionService sessionService;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    AccessTokenService accessTokenService;

    @Mock
    RoleService roleService;

    @InjectMocks
    AuthenticationAndAuthorizationServiceImpl authenticationAndAuthorizationService;

    @Test
    void shouldCreateMicroTimeManagementSessionLogin(){
        User user = UserTestFactory.existingAppUserEntity().build();
        Mockito.when(userService.loadUserByUsername(user.getUsername())).thenReturn(user);
        AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .token(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN).build();
        RefreshTokenDTO refreshTokenDTO = RefreshTokenDTO.builder()
                .token(AuthTestDataFactory.TokenConstants.JWT_SESSION_REFRESH_TOKEN)
                .accessTokenDTOList(List.of(accessTokenDTO))
                .activeAccessTokenDTO(accessTokenDTO)
                .build();
        SessionDTO sessionDTO = SessionDTO.builder()
                .refreshTokenDTO(refreshTokenDTO)
                .build();
        Mockito.when(bCryptPasswordEncoder.matches(authenticationRequestDTO.getPassword(), user.getPassword())).thenReturn(true);
        Mockito.when(sessionService.createNewSession(user)).thenReturn(sessionDTO);

        AuthenticationLoginResponseDTO responseDTO = authenticationAndAuthorizationService.microTimeManagementSessionLogin(authenticationRequestDTO);

        Assertions.assertThat(responseDTO.getAccessToken()).isEqualTo(accessTokenDTO.getToken());
        Assertions.assertThat(responseDTO.getRefreshToken()).isEqualTo(refreshTokenDTO.getToken());
    }

    @Test
    void shouldThrowBadPasswordException_whenMicroTimeManagementSessionLogin(){
        User user = UserTestFactory.existingAppUserEntity().build();
        Mockito.when(userService.loadUserByUsername(user.getUsername())).thenReturn(user);
        AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
        Mockito.when(bCryptPasswordEncoder.matches(authenticationRequestDTO.getPassword(), user.getPassword())).thenReturn(false);

        Assertions.assertThatExceptionOfType(MicroTimeManagementBadRequestException.class).isThrownBy(() -> {
            authenticationAndAuthorizationService.microTimeManagementSessionLogin(authenticationRequestDTO);
        }).withMessage("Invalid password. Please try again.");

    }


    @Test
    void shouldCreateMicroTimeManagementSessionRefresh(){
        AccessTokenRefreshRequestDTO accessTokenRefreshRequestDTO = AccessTokenRefreshRequestDTO.builder()
                .token(AuthTestDataFactory.TokenConstants.JWT_SESSION_REFRESH_TOKEN)
                .build();

        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .token(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN).build();
        RefreshTokenDTO refreshTokenDTO = RefreshTokenDTO.builder()
                .token(AuthTestDataFactory.TokenConstants.JWT_SESSION_REFRESH_TOKEN)
                .accessTokenDTOList(List.of(accessTokenDTO))
                .activeAccessTokenDTO(accessTokenDTO)
                .build();
        SessionDTO sessionDTO = SessionDTO.builder()
                .refreshTokenDTO(refreshTokenDTO)
                .build();
        Mockito.when(sessionService.refreshSession(accessTokenRefreshRequestDTO.getToken()))
                .thenReturn(sessionDTO);

        AuthenticationLoginResponseDTO responseDTO = authenticationAndAuthorizationService.microTimeManagementSessionRefresh(accessTokenRefreshRequestDTO);

        Assertions.assertThat(responseDTO.getAccessToken()).isEqualTo(accessTokenDTO.getToken());
        Assertions.assertThat(responseDTO.getRefreshToken()).isEqualTo(refreshTokenDTO.getToken());

    }

    @Test
    void shouldValidateCurrentUserSessionForAccessToken(){
        User user = UserTestFactory.existingAppUserEntity().build();
        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .token(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN).build();

        Mockito.when(jwtUtils.isTokenExpired(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(false);
        Mockito.when(jwtUtils.extractPrincipalFromToken(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(user.getUid());
        Mockito.when(userService.getUserByUid(user.getUid())).thenReturn(user);
        Mockito.when(jwtUtils.isValidTokenSubject(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN, user)).thenReturn(true);
        Mockito.when(accessTokenService.findAccessToken(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(accessTokenDTO);
        Mockito.when(roleService.getRoleNamesForIds(user.getRoles())).thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);

        ValidSessionDTO validSessionDTO = authenticationAndAuthorizationService.validateCurrentUserSessionForAccessToken(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN);

        Assertions.assertThat(validSessionDTO.getIsValidSession()).isEqualTo(Boolean.TRUE);
        Assertions.assertThat(validSessionDTO.getPrincipal()).isEqualTo(user);
        Assertions.assertThat(validSessionDTO.getError()).isNull();

    }

    @Test
    void shouldReturnSessionExpiredError_onValidateCurrentUserSessionForAccessToken(){
        Mockito.when(jwtUtils.isTokenExpired(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(true);

        ValidSessionDTO validSessionDTO = authenticationAndAuthorizationService.validateCurrentUserSessionForAccessToken(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN);

        Assertions.assertThat(validSessionDTO.getIsValidSession()).isEqualTo(Boolean.FALSE);
        Assertions.assertThat(validSessionDTO.getPrincipal()).isNull();
        Assertions.assertThat(validSessionDTO.getError()).isNotNull();
        Assertions.assertThat(validSessionDTO.getError()).isEqualTo(ErrorConstants.SESSION_EXPIRED);

    }

    @Test
    void shouldReturnInvalidSessionError_onValidateCurrentUserSessionForAccessToken(){
        Mockito.when(jwtUtils.isTokenExpired(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(false);
        Mockito.when(jwtUtils.extractPrincipalFromToken(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN)).thenReturn(null);

        ValidSessionDTO validSessionDTO = authenticationAndAuthorizationService.validateCurrentUserSessionForAccessToken(AuthTestDataFactory.TokenConstants.JWT_SESSION_ACCESS_TOKEN);

        Assertions.assertThat(validSessionDTO.getIsValidSession()).isEqualTo(Boolean.FALSE);
        Assertions.assertThat(validSessionDTO.getPrincipal()).isNull();
        Assertions.assertThat(validSessionDTO.getError()).isNotNull();
        Assertions.assertThat(validSessionDTO.getError()).isEqualTo(ErrorConstants.SESSION_TOKEN_INVALID);

    }


}
