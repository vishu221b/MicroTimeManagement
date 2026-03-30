package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.model.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken();

    Boolean revokeRefreshToken();

}
