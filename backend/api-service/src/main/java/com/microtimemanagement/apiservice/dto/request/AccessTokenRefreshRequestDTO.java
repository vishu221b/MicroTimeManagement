package com.microtimemanagement.apiservice.dto.request;

import lombok.Data;

@Data
public class AccessTokenRefreshRequestDTO {
    private String token;
}
