package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.SessionRepository;
import com.microtimemanagement.apiservice.service.*;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final JsonWebTokenService jsonWebTokenService;
    private final UserService userService;
    private final RoleService roleService;
    private final SessionConverter sessionConverter;
    private final SessionRepository sessionRepository;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SessionDTO createNewSession(User user) {
        Optional<Session> session = sessionRepository.findByUserAndIsActiveTrue(user);
        Session existing = null;
        // Expire any existing session before generating a new one
        if(session.isPresent()){
            existing = session.get();
            existing.setIsActive(Boolean.FALSE);
            existing.getRefreshToken().setIsActive(Boolean.FALSE);
            existing.getRefreshToken().getAccessTokens().forEach(
                    accessToken -> {
                        if(accessToken.getIsActive().equals(Boolean.TRUE)) {
                            accessToken.setIsActive(Boolean.FALSE);
                        }
                    });
            refreshTokenService.saveRefreshToken(existing.getRefreshToken());
            accessTokenService.saveAccessTokens(existing.getRefreshToken().getAccessTokens());
        }
        Session newSession = Session.builder()
                .user(user)
                .refreshToken(refreshTokenService.createRefreshToken(
                        SessionPrincipalDTO.builder()
                                .uid(user.getUid())
                                .authorities(user.getRoles())
                                .build()
                )).build();
        if(null != existing){
            // Delete Older active Session along with creation of new one
            newSession = sessionRepository.saveAll(List.of(newSession, existing))
                    .stream()
                    .filter(currentSession -> currentSession.getIsActive().equals(Boolean.TRUE))
                    .toList()
                    .get(0);
        }else{
            newSession = sessionRepository.save(newSession);
        }
        return sessionConverter.toDTO(newSession);
    }

    @Override
    @Transactional
    public void destroySession(String token) {
        AccessToken accessToken = accessTokenService.findByToken(token);
        RefreshToken refreshToken = accessToken.getRefreshToken();
        Session session = refreshToken.getSession();
        accessToken.setIsActive(Boolean.FALSE);
        refreshToken.setIsActive(Boolean.FALSE);
        session.setIsActive(Boolean.FALSE);
        sessionRepository.save(session);
        refreshTokenService.saveRefreshToken(refreshToken);
        accessTokenService.saveAccessTokens(List.of(accessToken));
        log.info("User logged out.");
    }

    /**
     *
     */
    @Override
    public ValidSessionDTO validateSessionForAccessToken(String token) {
        Boolean isValidToken = Boolean.FALSE;
        final Boolean isExpired = jsonWebTokenService.isJwtTokenExpired(token);
        String error = null;
        if(isExpired){
            error = ErrorConstants.SESSION_EXPIRED;
            return ValidSessionDTO.builder()
                    .isValidSession(isValidToken)
                    .error(error)
                    .build();
        }

        final String principal = jsonWebTokenService.getPrincipalSubjectForToken(token);

        User user = null;
        if(null!=principal && !principal.isEmpty()){
            user = userService.getUserByUid(principal);
            isValidToken = jsonWebTokenService.tokenSubjectIsValid(token, user);
        }
        if(isValidToken){
            AccessTokenDTO accessTokenDTO = accessTokenService.findAccessToken(token);
            if(null == accessTokenDTO){
                log.error("No session found for token: {}, giving error", token);
                return ValidSessionDTO.builder()
                        .isValidSession(Boolean.FALSE)
                        .principal(null)
                        .error(ErrorConstants.SESSION_TOKEN_INVALID)
                        .build();
            }
            // Convert role from Ids to Names for correct filter chain matching
            user.setRoles(
                    roleService.getRoleNamesForIds(user.getRoles())
            );
        }else {
            error = ErrorConstants.SESSION_TOKEN_INVALID;
        }
        final Boolean isValid = null!=principal && isValidToken;
        log.info("isValidSession for isValid: {} and user: {} with error: {}", isValid, user, error);
        return ValidSessionDTO.builder()
                .isValidSession(isValid)
                .principal(user)
                .error(error)
                .build();

    }

    @Override
    public SessionDTO refreshSession(String token) {
        RefreshToken refreshToken = refreshTokenService.findEntityByActiveToken(token);
        if(null == refreshToken){
            throw new MicroTimeManagementNotFoundException(ErrorConstants.SESSION_TOKEN_INVALID);
        }
        Session activeSession = refreshToken.getSession();
        if(refreshToken.getExpiresAt().getTime() <= System.currentTimeMillis() || activeSession.getIsActive().equals(Boolean.FALSE))
            throw new MicroTimeManagementNotFoundException(ErrorConstants.SESSION_EXPIRED);
        AccessToken accessToken = accessTokenService.createAccessToken(
                SessionPrincipalDTO.builder()
                        .uid(activeSession.getUser().getUid())
                        .authorities(roleService.getRoleNamesForIds(activeSession.getUser().getRoles()))
                        .build()
        );
        List<AccessToken> accessTokens = refreshToken.getAccessTokens();
        accessTokens.forEach(t -> t.setIsActive(Boolean.FALSE));
        accessTokenService.saveAccessTokens(accessTokens);
        accessTokens.add(accessToken);
        refreshToken.setAccessTokens(accessTokens);
        refreshTokenService.saveRefreshToken(refreshToken);
        return sessionConverter.toDTO(activeSession);
    }
}
