package com.microtimemanagement.apiservice.service;


import com.microtimemanagement.apiservice.converter.SessionConverter;
import com.microtimemanagement.apiservice.repository.SessionRepository;
import com.microtimemanagement.apiservice.service.impl.SessionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionServiceImplTest {

    @Mock
    SessionRepository sessionRepository;

    @Mock
    SessionConverter sessionConverter;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    AccessTokenService accessTokenService;

    @Mock
    RoleService roleService;

    @InjectMocks
    SessionServiceImpl sessionService;

    @Test
    void shouldCreateNewSession(){}

    @Test
    void shouldDestroyExistingSession(){}

    @Test
    void shouldGetSessionByToken(){}

    @Test
    void shouldRefreshSession(){}

}
