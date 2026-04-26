package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionTokenDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.SessionRepository;
import com.microtimemanagement.apiservice.service.RefreshTokenService;
import com.microtimemanagement.apiservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionConverter sessionConverter;
    private final SessionRepository sessionRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SessionDTO createNewSession(User user) {

        Optional<Session> session = sessionRepository.findByUserAndIsActiveTrue(user);
        Session existing = null;

        // Invalidate any existing session before generating a new one

        if(session.isPresent()){
            existing = session.get();
            revokeSession(existing);
            refreshTokenService.revokeRefreshToken(existing.getRefreshToken());
        }

        Session newSession = Session.builder()
                .user(user)
                .refreshToken(refreshTokenService.createRefreshToken(
                        SessionPrincipalDTO.builder()
                                .uid(user.getUid())
                                .authorities(user.getRoles())
                                .build()
                )).build();
        newSession = sessionRepository.save(newSession);
        return sessionConverter.toDTO(newSession);
    }

    @Override
    @Transactional
    public void destroySessionForAccessToken(String token) {
        if(null!=token && !token.isEmpty() && !token.isBlank()){
            revokeSession(refreshTokenService.revokeRefreshTokenUsingAccessToken(token).getSession());
        }
        log.info("User logged out.");
    }

    private void revokeSession(Session session){
        if(null != session){
            session.setIsActive(Boolean.FALSE);
            sessionRepository.save(session);
        }
    }

    @Override
    public ValidSessionTokenDTO validateSessionForAccessToken(String token) {
        return refreshTokenService.validateAccessToken(token);
    }

    @Override
    public SessionDTO refreshSession(String token) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(token);

        if(refreshToken.getSession().getIsActive().equals(Boolean.FALSE))
            throw new MicroTimeManagementNotFoundException(ErrorConstants.SESSION_EXPIRED);

        refreshToken = refreshTokenService.refreshSessionForRefreshToken(refreshToken);
        log.info("Refreshed session for refreshToken: {}", refreshToken);
        return sessionConverter.toDTO(refreshToken.getSession());
    }
}
