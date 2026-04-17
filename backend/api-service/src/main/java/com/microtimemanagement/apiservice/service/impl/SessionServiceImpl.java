package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.SessionRepository;
import com.microtimemanagement.apiservice.service.AccessTokenService;
import com.microtimemanagement.apiservice.service.RefreshTokenService;
import com.microtimemanagement.apiservice.service.SessionService;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    private final SessionConverter sessionConverter;

    private final RefreshTokenService refreshTokenService;

    private final AccessTokenService accessTokenService;

    private final JwtUtils jwtUtils;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SessionDTO createNewSession(User user) {
        Optional<Session> session = sessionRepository.findByUserAndIsActiveTrue(user);
        Session existing = null;
        // Expire any existing session before generating a new one
        if(session.isPresent()){
            existing = session.get();
            existing.setIsActive(Boolean.FALSE);
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
    public void destroySession(String token) {
        Optional<Session> session = sessionRepository.findByRefreshTokenAndIsActiveTrue(RefreshToken.builder().accessTokens(List.of(AccessToken.builder().token(token).build())).build());
        if(session.isPresent()){
            Session updatedSession = session.get();
            updatedSession.setIsActive(Boolean.FALSE);
            sessionRepository.save(updatedSession);
        }
    }

    @Override
    public SessionDTO getByToken(String token) {
        RefreshToken refreshToken = refreshTokenService.findByActiveAccessToken(token);
        AccessToken accessToken = refreshToken.getActiveAccessToken();
        log.info("Retrieved refreshToken and accessToken: {} , {}", refreshToken, accessToken);
        Optional<Session> session = sessionRepository.findByRefreshTokenAndIsActiveTrue(
                RefreshToken.builder()
                        .accessTokens(
                                List.of(AccessToken.builder().token(token).build())
                        )
                        .build());
        return session.map(sessionConverter::toDTO).orElse(null);
    }

    /**
     *
     */
    @Override
    public void validateSession(Session session) {
        //TODO
    }
}
