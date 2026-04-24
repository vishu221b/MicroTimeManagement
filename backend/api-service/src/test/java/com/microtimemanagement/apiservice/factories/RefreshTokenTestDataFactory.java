package com.microtimemanagement.apiservice.factories;

import com.microtimemanagement.apiservice.model.RefreshToken;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RefreshTokenTestDataFactory {
    public static RefreshToken.RefreshTokenBuilder<?,?> mockRefreshTokenEntity() {
        return RefreshToken.builder()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .token(UUID.randomUUID().toString())
                .expiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)))
                .createdAt(new Date())
                .lastUpdatedAt(new Date())
                .isActive(Boolean.TRUE);
    }
}
