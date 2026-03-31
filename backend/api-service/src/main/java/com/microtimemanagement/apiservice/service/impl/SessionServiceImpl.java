package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.SessionRepository;
import com.microtimemanagement.apiservice.service.RefreshTokenService;
import com.microtimemanagement.apiservice.service.SessionService;
import com.microtimemanagement.apiservice.utils.AuthUtils;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    private final SessionConverter sessionConverter;

    private final RefreshTokenService refreshTokenService;

    private final JwtUtils jwtUtils;

    @Override
    public SessionDTO createNewSession(User user) {
        Optional<Session> session = sessionRepository.findByUserAndIsActiveTrue(user);
        // Expire any existing session before generating a new one
        session.ifPresent(value -> value.setIsActive(Boolean.FALSE));
        Session newSession = Session.builder()
                .user(user)
                .refreshToken(refreshTokenService.createRefreshToken()).build();
        newSession = sessionRepository.save(newSession);
        return sessionConverter.toDTO(newSession);
    }

    @Override
    public void destroySession(String token) {
        Optional<Session> session = sessionRepository.findByTokenAndIsActiveTrue(token);
        if(session.isPresent()){
            Session updatedSession = session.get();
            updatedSession.setIsActive(Boolean.FALSE);
            sessionRepository.save(updatedSession);
        }
    }

    @Override
    public SessionDTO getByToken(String token) {
        Optional<Session> session = sessionRepository.findByTokenAndIsActiveTrue(token);
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
