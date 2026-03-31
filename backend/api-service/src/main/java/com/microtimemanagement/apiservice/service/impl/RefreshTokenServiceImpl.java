package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.dto.SessionPrincipalDTO;
import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.RefreshTokenRepository;
import com.microtimemanagement.apiservice.service.AccessTokenService;
import com.microtimemanagement.apiservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.CalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final AccessTokenService accessTokenService;
    /**
     * @return
     */
    @Override
    public RefreshToken createRefreshToken(SessionPrincipalDTO sessionPrincipalDTO) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)))
                .accessTokens(List.of(accessTokenService.createAccessToken(sessionPrincipalDTO)))
                .build();
        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Saved refresh token: {}", refreshToken);
        return refreshToken;
    }

    /**
     * @return
     */
    @Override
    public Boolean revokeRefreshToken(String token) {
        return null;
    }

    /**
     * @param refreshToken
     */
    @Override
    public void validateRefreshToken(String refreshToken) {

    }
}
