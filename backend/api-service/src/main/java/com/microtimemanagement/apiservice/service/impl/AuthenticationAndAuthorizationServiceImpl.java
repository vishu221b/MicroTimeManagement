package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.dto.request.AccessTokenRefreshRequestDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.service.AuthenticationAndAuthorizationService;
import com.microtimemanagement.apiservice.service.SessionService;
import com.microtimemanagement.apiservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationAndAuthorizationServiceImpl implements AuthenticationAndAuthorizationService {

    private final UserService userService;
    private final SessionService sessionService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public AuthenticationLoginResponseDTO microTimeManagementSessionLogin(
            AuthenticationRequestDTO authenticationRequestDTO
    ) {
        User user = userService.loadUserByUsername(authenticationRequestDTO.getUsername());
        if(bCryptPasswordEncoder.matches(authenticationRequestDTO.getPassword(), user.getPassword())){
            SessionDTO sessionDTO = sessionService.createNewSession(user);
            return AuthenticationLoginResponseDTO.builder()
                    .accessToken(sessionDTO.getRefreshTokenDTO().getActiveAccessTokenDTO().getToken())
                    .refreshToken(sessionDTO.getRefreshTokenDTO().getToken())
                    .build();
        }
        throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_PASSWORD_VALUE);
    }

    @Override
    public void microTimeManagementSessionLogout(String token) {
        sessionService.destroySession(token);
    }

    @Override
    public ValidSessionDTO validateCurrentUserSessionForAccessToken(String token) {
        return sessionService.validateSessionForAccessToken(token);
    }

    @Override
    public AuthenticationLoginResponseDTO microTimeManagementSessionRefresh(
            AccessTokenRefreshRequestDTO accessTokenRefreshRequestDTO) {
        SessionDTO sessionDTO = sessionService.refreshSession(accessTokenRefreshRequestDTO.getToken());
        return AuthenticationLoginResponseDTO.builder()
                .accessToken(sessionDTO.getRefreshTokenDTO().getActiveAccessTokenDTO().getToken())
                .refreshToken(sessionDTO.getRefreshTokenDTO().getToken())
                .build();
    }
}
