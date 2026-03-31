package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;

public interface AuthenticationAndAuthorizationService {

    AuthenticationLoginResponseDTO microTimeManagementSessionLogin(AuthenticationRequestDTO authenticationRequestDTO);

    GenericMessageResponseDTO<?> microTimeManagementSessionLogout(String substring);

    ValidSessionDTO validateCurrentUserSessionForAccessToken(String token);
}
