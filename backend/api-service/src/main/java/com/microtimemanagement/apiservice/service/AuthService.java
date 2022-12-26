package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.dto.ValidSessionDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLogoutResponseDTO;

public interface AuthService {

    AuthenticationLoginResponseDTO generateToken(AuthenticationRequestDTO authenticationRequestDTO);

    AuthenticationLogoutResponseDTO expireToken(String substring);

    ValidSessionDTO isValidSessionToken(String token);
}
