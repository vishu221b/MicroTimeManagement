package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.dto.request.AccessTokenRefreshRequestDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import jakarta.validation.Valid;

public interface AuthenticationAndAuthorizationService {

    AuthenticationLoginResponseDTO microTimeManagementSessionLogin(AuthenticationRequestDTO authenticationRequestDTO);

    void microTimeManagementSessionLogout(String substring);

    ValidSessionDTO validateCurrentUserSessionForAccessToken(String token);

    AuthenticationLoginResponseDTO microTimeManagementSessionRefresh(@Valid AccessTokenRefreshRequestDTO accessTokenRefreshRequestDTO);
}
