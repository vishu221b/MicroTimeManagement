package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.repository.AccessTokenRepository;
import com.microtimemanagement.apiservice.service.AccessTokenService;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    private final AccessTokenRepository accessTokenRepository;

    private final JwtUtils jwtUtils;

    /**
     * @return
     */
    @Override
    public AccessToken createAccessToken() {
        String newToken=jwtUtils.generateToken(principal, principalAuthorities);
        AccessToken accessToken = AccessToken.builder()
                .token(newToken)
                .expiresAt()
                .build();
        accessToken = accessTokenRepository.save(accessToken);
        log.info("Built access token: {}", accessToken);
        return accessToken;
    }

    /**
     * @param accessToken
     * @return
     */
    @Override
    public Boolean revokeAccessToken(AccessToken accessToken) {
        return null;
    }

    /**
     * @param accessToken
     */
    @Override
    public void validateAccessToken(AccessToken accessToken) {

    }
}
