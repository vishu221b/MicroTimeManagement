package com.microtimemanagement.apiservice.dto.request;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationRequestDTO {

    @NotNull(message = ErrorConstants.USERNAME_CANNOT_BE_NULL)
    @NotBlank(message = ErrorConstants.USERNAME_CANNOT_BE_BLANK)
    private String username;

    @NotNull(message = ErrorConstants.PASSWORD_CANNOT_BE_NULL)
    @NotBlank(message = ErrorConstants.PASSWORD_CANNOT_BE_BLANK)
    private String password;

}
