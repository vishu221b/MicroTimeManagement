package com.microtimemanagement.apiservice.dto.request;

import lombok.Data;

@Data
public class AuthenticationRequestDTO {

    private String username;

    private String password;

}
