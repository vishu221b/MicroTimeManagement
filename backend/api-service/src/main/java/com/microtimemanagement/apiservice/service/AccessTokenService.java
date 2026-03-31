package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.model.AccessToken;

public interface AccessTokenService {

    AccessToken createAccessToken();

    Boolean revokeAccessToken(AccessToken accessToken);

    void validateAccessToken(AccessToken accessToken);

}
