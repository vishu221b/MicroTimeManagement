package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.model.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(SessionPrincipalDTO sessionPrincipalDTO);

    Boolean revokeRefreshToken(String token);

    void validateRefreshToken(String token);

    RefreshToken findByActiveAccessToken(String token);
}
