package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.ValidSessionTokenDTO;
import com.microtimemanagement.apiservice.dto.request.JwtCreationRequestDTO;
import com.microtimemanagement.apiservice.model.User;

public interface JsonWebTokenService {
    public String createNewToken(JwtCreationRequestDTO requestDTO);

    public String getPrincipalSubjectForToken(String token);

    public Boolean isJwtTokenExpired(String token);

    public Boolean tokenSubjectIsValid(String token, User user);

    ValidSessionTokenDTO validateJwtSessionAccessToken(String token);
}
