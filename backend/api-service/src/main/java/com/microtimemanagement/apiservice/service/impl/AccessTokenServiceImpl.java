package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.request.JwtCreationRequestDTO;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.repository.AccessTokenRepository;
import com.microtimemanagement.apiservice.service.AccessTokenService;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    public AccessToken createAccessToken(SessionPrincipalDTO sessionPrincipalDTO) {
        Date tokenExpiry = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(5));
        String newToken=jwtUtils.generateToken(
                JwtCreationRequestDTO.builder()
                        .principal(sessionPrincipalDTO.getUid())
                        .principalAuthorities(sessionPrincipalDTO.getAuthorities())
                        .additionalClaims(null) // Nothing at the moment
                        .expiry(tokenExpiry)
                        .build()
        );
        AccessToken accessToken = AccessToken.builder()
                .token(newToken)
                .expiresAt(tokenExpiry)
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
    public Boolean revokeAccessToken(String accessToken) {
        return null;
    }

    /**
     * @param accessToken
     */
    @Override
    public void validateAccessToken(String accessToken) {

    }
}
