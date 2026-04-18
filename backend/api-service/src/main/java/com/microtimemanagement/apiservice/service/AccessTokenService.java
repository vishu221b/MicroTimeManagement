package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.model.AccessToken;

import java.util.List;

public interface AccessTokenService {

    AccessToken createAccessToken(SessionPrincipalDTO sessionPrincipalDTO);

    List<AccessToken> saveAccessTokens(List<AccessToken> accessToken);

    Boolean revokeAccessToken(String accessToken);

    AccessTokenDTO findAccessToken(String accessToken);

    AccessToken findByToken(String token);
}
