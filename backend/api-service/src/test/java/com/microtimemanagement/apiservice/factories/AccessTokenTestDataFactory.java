package com.microtimemanagement.apiservice.factories;

import com.microtimemanagement.apiservice.model.AccessToken;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AccessTokenTestDataFactory {

    public static AccessToken.AccessTokenBuilder<?,?> newAccessTokenEntity(){
        return AccessToken.builder()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .token(UUID.randomUUID().toString())
                .expiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)))
                .createdAt(new Date())
                .lastUpdatedAt(new Date())
                .isActive(Boolean.TRUE);
    }
}
