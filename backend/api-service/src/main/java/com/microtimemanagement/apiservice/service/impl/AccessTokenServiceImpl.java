package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.AccessTokenConverter;
import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionTokenDTO;
import com.microtimemanagement.apiservice.dto.request.JwtCreationRequestDTO;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.repository.AccessTokenRepository;
import com.microtimemanagement.apiservice.service.AccessTokenService;
import com.microtimemanagement.apiservice.service.JsonWebTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    private final JsonWebTokenService jsonWebTokenService;
    private final AccessTokenRepository accessTokenRepository;
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

    private AccessToken saveAccessToken(AccessToken accessToken) {
        return accessTokenRepository.save(accessToken);
    }

    /**
     * @param token
     * @return
     */
    @Override
    public AccessToken revokeAccessToken(String token) {
        AccessToken accessToken = findByToken(token);
        if(null != accessToken){
            accessToken.setIsActive(Boolean.FALSE);
            return saveAccessToken(accessToken);
        }
        return null;
    }

    @Override
    @Transactional
    public Boolean revokeAccessTokens(List<AccessToken> accessTokens) {
        if(null!=accessTokens && !accessTokens.isEmpty()){
            accessTokens.forEach(
                    accessToken -> {
                        if(accessToken.getIsActive().equals(Boolean.TRUE))
                            accessToken.setIsActive(Boolean.FALSE);
                    }
            );
            saveAccessTokens(accessTokens);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
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

    @Override
    public ValidSessionTokenDTO validateAccessToken(String token) {

        AccessTokenDTO accessTokenDTO = findAccessToken(token);

        if(null == accessTokenDTO){
            log.error("No session found for token: {}, giving error", token);
            return ValidSessionTokenDTO.builder()
                    .isValidSession(Boolean.FALSE)
                    .principal(null)
                    .error(ErrorConstants.SESSION_TOKEN_INVALID)
                    .build();
        }

        if(null != accessTokenDTO.getExpiresAt()
                && accessTokenDTO.getExpiresAt().getTime() <= System.currentTimeMillis()){
            log.info("Access token expired at {}", accessTokenDTO.getExpiresAt());
            return ValidSessionTokenDTO.builder()
                    .isValidSession(Boolean.FALSE)
                    .principal(null)
                    .error(ErrorConstants.SESSION_EXPIRED)
                    .build();
        }

        return jsonWebTokenService.validateJwtSessionAccessToken(token);
    }
}
