package com.microtimemanagement.apiservice;

import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.model.User;

public class TestDataFactory {
    public static NewUserRequestDTO getNewUserRequestDTO(){
        return NewUserRequestDTO.builder()
                .firstName("Test First")
                .lastName("Test Last")
                .email("test@test.com")
                .dateOfBirth("12-12-2002")
                .username("testersFirst")
                .password("Test123")
                .build();
    }

    public static User getUser(){
        return User.builder()
                .firstName("Test First")
                .lastName("Test Last")
                .email("test@test.com")
                .dateOfBirth("12-12-2002")
                .username("testersFirst")
                .password("Test123")
                .build();
    }
}
