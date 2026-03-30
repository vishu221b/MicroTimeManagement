package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.dto.SessionDTO;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.SessionRepository;
import com.microtimemanagement.apiservice.service.SessionService;
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

    private final JwtUtils jwtUtils;

    @Override
    public SessionDTO createNewSession(User user) {
        Session session = sessionRepository.findByUserIdAndIsActiveTrue(user.getId());
        if(null!=session && !jwtUtils.isTokenExpired(session.getToken())){

            return sessionConverter.toDTO(session);
        }
        // TODO: Add a session token for revoking and re-issuing fresh tokens on the go
        session = sessionRepository.save(
                Session.builder()
                        .token(jwtUtils.generateToken(user, null))
                        .createdAt(new Date(System.currentTimeMillis()))
                        .lastUpdatedAt(new Date(System.currentTimeMillis()))
                        .isActive(Boolean.TRUE)
                        .userId(user.getId())
                        .build());
        sessionRepository.save(session);
        return sessionConverter.toDTO(session);
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
}
