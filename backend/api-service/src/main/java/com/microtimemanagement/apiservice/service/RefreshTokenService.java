package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionTokenDTO;
import com.microtimemanagement.apiservice.model.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(SessionPrincipalDTO sessionPrincipalDTO);

    RefreshToken saveRefreshToken(RefreshToken refreshToken);

    Boolean revokeRefreshToken(RefreshToken refreshToken);

    RefreshToken validateRefreshToken(String token);

    RefreshToken findByActiveAccessToken(String token);

    RefreshToken findEntityByActiveToken(String token);

    RefreshToken refreshSessionForRefreshToken(RefreshToken refreshToken);

    RefreshToken revokeRefreshTokenUsingAccessToken(String token);

    ValidSessionTokenDTO validateAccessToken(String token);
}
