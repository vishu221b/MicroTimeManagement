package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionTokenDTO;
import com.microtimemanagement.apiservice.model.User;

public interface SessionService {

    SessionDTO createNewSession(User user);

    void destroySessionForAccessToken(String token);

    ValidSessionTokenDTO validateSessionForAccessToken(String token);

    SessionDTO refreshSession(String token);
}
