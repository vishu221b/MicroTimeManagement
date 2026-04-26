package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.dto.entity.ValidSessionTokenDTO;
import com.microtimemanagement.apiservice.dto.request.AccessTokenRefreshRequestDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import jakarta.validation.Valid;

public interface AuthenticationAndAuthorizationService {

    AuthenticationLoginResponseDTO microTimeManagementSessionLogin(AuthenticationRequestDTO authenticationRequestDTO);

    void microTimeManagementSessionLogout(String substring);

    ValidSessionTokenDTO validateCurrentUserSessionForAccessToken(String token);

    AuthenticationLoginResponseDTO microTimeManagementSessionRefresh(@Valid AccessTokenRefreshRequestDTO accessTokenRefreshRequestDTO);
}
