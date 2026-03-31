package com.microtimemanagement.apiservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationLoginResponseDTO {

    private String accessToken;

    private String refreshToken;

}
