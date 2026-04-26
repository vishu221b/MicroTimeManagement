package com.microtimemanagement.apiservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessTokenRefreshRequestDTO {
    private String token;
}
