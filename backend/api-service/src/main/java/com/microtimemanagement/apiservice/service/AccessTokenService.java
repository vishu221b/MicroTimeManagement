package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.model.AccessToken;

public interface AccessTokenService {

    AccessToken createAccessToken(SessionPrincipalDTO sessionPrincipalDTO);

    Boolean revokeAccessToken(String accessToken);

    AccessTokenDTO findAccessToken(String accessToken);

    AccessToken findByToken(String token);
}
