package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.model.AccessToken;

public interface AccessTokenService {

    AccessToken createAccessToken();

    Boolean revokeAccessToken(AccessToken accessToken);

    Boolean validateAccessToken(AccessToken accessToken);

}
