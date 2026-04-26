package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.converter.AccessTokenConverter;
import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.dto.request.JwtCreationRequestDTO;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.repository.AccessTokenRepository;
import com.microtimemanagement.apiservice.service.AccessTokenService;
import com.microtimemanagement.apiservice.service.JsonWebTokenService;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    private final AccessTokenRepository accessTokenRepository;

    private final JsonWebTokenService jsonWebTokenService;

    private final AccessTokenConverter accessTokenConverter;

    /**
     * @param sessionPrincipalDTO The DTO containing subject information used to create {@link AccessToken}
     * @return {@link AccessToken}
     * @see SessionPrincipalDTO
     */
    @Override
    public AccessToken createAccessToken(SessionPrincipalDTO sessionPrincipalDTO) {
        Date tokenExpiry = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(5));
        String newToken=jsonWebTokenService.createNewToken(
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
     * @param accessTokens The Access Tokens List that will be saved to the Database.
     * @return A {@link List} of {@link AccessToken}
     */
    @Override
    public List<AccessToken> saveAccessTokens(List<AccessToken> accessTokens) {
        return accessTokenRepository.saveAll(accessTokens);
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
    public AccessTokenDTO findAccessToken(String accessToken) {
        AccessToken validAccessToken = findByToken(accessToken);
        return accessTokenConverter.toDTO(validAccessToken);

    }

    @Override
    public AccessToken findByToken(String token) {
        AccessToken accessToken = accessTokenRepository.findByTokenAndIsActiveTrue(token);
        log.info("Found by token: {}", accessToken);
        return accessToken;
    }
}
