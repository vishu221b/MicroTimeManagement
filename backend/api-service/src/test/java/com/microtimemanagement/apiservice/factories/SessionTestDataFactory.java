package com.microtimemanagement.apiservice.factories;

import com.microtimemanagement.apiservice.model.Session;

import java.util.Date;
import java.util.UUID;

public class SessionTestDataFactory {

    public static Session.SessionBuilder<?,?> mockSessionEntity(){
        return Session.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .isActive(Boolean.TRUE);
    }

}
