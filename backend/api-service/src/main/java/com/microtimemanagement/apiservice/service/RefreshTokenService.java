package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.model.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(SessionPrincipalDTO sessionPrincipalDTO);

    RefreshToken saveRefreshToken(RefreshToken refreshToken);

    Boolean revokeRefreshToken(String token);

    void validateRefreshToken(String token);

    RefreshToken findByActiveAccessToken(String token);

    RefreshToken findEntityByActiveToken(String token);
}
