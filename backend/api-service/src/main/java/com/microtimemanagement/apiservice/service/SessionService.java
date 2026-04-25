package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.model.User;

public interface SessionService {

    SessionDTO createNewSession(User user);

    void destroySession(String token);

    ValidSessionDTO validateSessionForAccessToken(String token);

    SessionDTO refreshSession(String token);
}
