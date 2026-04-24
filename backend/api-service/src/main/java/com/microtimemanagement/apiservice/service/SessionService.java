package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;

public interface SessionService {

    SessionDTO createNewSession(User user);

    void destroySession(String token);

    SessionDTO getByAccessToken(String token);

    void validateSession(Session session);

    SessionDTO refreshSession(String token);
}
