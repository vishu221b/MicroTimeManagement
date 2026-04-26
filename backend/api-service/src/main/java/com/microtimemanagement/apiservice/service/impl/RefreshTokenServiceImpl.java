package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionTokenDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.RefreshTokenRepository;
import com.microtimemanagement.apiservice.service.AccessTokenService;
import com.microtimemanagement.apiservice.service.RefreshTokenService;
import com.microtimemanagement.apiservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RoleService roleService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    /**
     * @return
     */
    @Override
    public RefreshToken createRefreshToken(SessionPrincipalDTO sessionPrincipalDTO) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)))
                .accessTokens(List.of(accessTokenService.createAccessToken(sessionPrincipalDTO)))
                .build();
        refreshToken = saveRefreshToken(refreshToken);
        log.info("Saved refresh token: {}", refreshToken);
        return refreshToken;
    }

    @Override
    public RefreshToken saveRefreshToken(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }


    /**
     * @return
     */
    @Override
    @Transactional
    public Boolean revokeRefreshToken(RefreshToken refreshToken) {
        if(null!=refreshToken){
            refreshToken.setIsActive(Boolean.FALSE);
            refreshToken=saveRefreshToken(refreshToken);
            return accessTokenService.revokeAccessTokens(refreshToken.getAccessTokens());
        }
        return Boolean.FALSE;
    }

    /**
     * @param token Refresh Token to be validated
     */
    @Override
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = findEntityByActiveToken(token);
        if(null == refreshToken)
            throw new MicroTimeManagementNotFoundException(ErrorConstants.SESSION_TOKEN_INVALID);
        if(refreshToken.getExpiresAt().getTime() <= System.currentTimeMillis())
            throw new MicroTimeManagementNotFoundException(ErrorConstants.SESSION_EXPIRED);
        return refreshToken;
    }

    @Override
    public RefreshToken findByActiveAccessToken(String token) {
        AccessToken accessToken = accessTokenService.findByToken(token);
        return refreshTokenRepository.findByAccessTokensAndIsActiveTrue(List.of(accessToken.getId()));
    }

    @Override
    public RefreshToken findEntityByActiveToken(String token) {
        return refreshTokenRepository.findByTokenAndIsActiveTrue(token);
    }

    @Override
    public RefreshToken refreshSessionForRefreshToken(RefreshToken refreshToken) {
        User principal = refreshToken.getSession().getUser();
        AccessToken accessToken = accessTokenService.createAccessToken(
                SessionPrincipalDTO.builder()
                        .uid(principal.getUid())
                        .authorities(roleService.getRoleNamesForIds(principal.getRoles()))
                        .build()
        );
        List<AccessToken> accessTokens = refreshToken.getAccessTokens();
        accessTokenService.revokeAccessTokens(accessTokens);
        accessTokens.add(accessToken);
        refreshToken.setAccessTokens(accessTokens);
        saveRefreshToken(refreshToken);

        return refreshToken;
    }

    @Override
    public RefreshToken revokeRefreshTokenUsingAccessToken(String token) {
        AccessToken accessToken = accessTokenService.revokeAccessToken(token);
        revokeRefreshToken(accessToken.getRefreshToken());
        return accessToken.getRefreshToken();
    }

    @Override
    public ValidSessionTokenDTO validateAccessToken(String token) {
        return accessTokenService.validateAccessToken(token);
    }
}
