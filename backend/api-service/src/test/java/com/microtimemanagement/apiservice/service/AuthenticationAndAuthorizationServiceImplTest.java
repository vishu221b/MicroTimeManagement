package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.RefreshTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.request.AccessTokenRefreshRequestDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.factories.AuthTestDataFactory;
import com.microtimemanagement.apiservice.factories.UserTestFactory;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.service.impl.AuthenticationAndAuthorizationServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

/**
 * @author vishal.dogra
 * @since 1.0.0
 */

@DisplayName("Authentication and Authorization Service Tests")
@ExtendWith(MockitoExtension.class)
public class AuthenticationAndAuthorizationServiceImplTest {

    @Mock
    UserService userService;

    @Mock
    SessionService sessionService;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    AuthenticationAndAuthorizationServiceImpl authenticationAndAuthorizationService;

    @Test
    @DisplayName("Should create mtm session login Successfully.")
    void shouldCreateMicroTimeManagementSessionLogin(){
        User user = UserTestFactory.existingAppUserEntity().build();
        Mockito.when(userService.loadUserByUsername(user.getUsername())).thenReturn(user);
        AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .token(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN)
                .isActive(Boolean.TRUE).build();
        RefreshTokenDTO refreshTokenDTO = RefreshTokenDTO.builder()
                .token(AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN)
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
    @DisplayName("Should throw Bad request exception with message 'Invalid password' when the login password is incorrect.")
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
        }).withMessage(ErrorConstants.INVALID_PASSWORD_VALUE);

    }


    @Test
    @DisplayName("Should refresh the user session.")
    void shouldCreateMicroTimeManagementSessionRefresh(){
        AccessTokenRefreshRequestDTO accessTokenRefreshRequestDTO = AccessTokenRefreshRequestDTO.builder()
                .token(AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN)
                .build();

        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .token(AuthTestDataFactory.MockConstants.JWT_SESSION_ACCESS_TOKEN)
                .isActive(Boolean.TRUE)
                .build();
        RefreshTokenDTO refreshTokenDTO = RefreshTokenDTO.builder()
                .token(AuthTestDataFactory.MockConstants.JWT_SESSION_REFRESH_TOKEN)
                .accessTokenDTOList(List.of(accessTokenDTO))
                .activeAccessTokenDTO(accessTokenDTO)
                .build();
        SessionDTO sessionDTO = SessionDTO.builder()
                .refreshTokenDTO(refreshTokenDTO)
                .build();

        Mockito.when(sessionService.refreshSession(accessTokenRefreshRequestDTO.getToken()))
                .thenReturn(sessionDTO);

        AuthenticationLoginResponseDTO responseDTO = authenticationAndAuthorizationService
                .microTimeManagementSessionRefresh(accessTokenRefreshRequestDTO);

        Assertions.assertThat(responseDTO.getAccessToken()).isEqualTo(accessTokenDTO.getToken());
        Assertions.assertThat(responseDTO.getRefreshToken()).isEqualTo(refreshTokenDTO.getToken());

    }

}
