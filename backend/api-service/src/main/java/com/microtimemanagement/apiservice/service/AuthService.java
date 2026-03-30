package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;

public interface AuthService {

    AuthenticationLoginResponseDTO generateToken(AuthenticationRequestDTO authenticationRequestDTO);

    GenericMessageResponseDTO<?> expireToken(String substring);

    ValidSessionDTO isValidSessionToken(String token);
}
