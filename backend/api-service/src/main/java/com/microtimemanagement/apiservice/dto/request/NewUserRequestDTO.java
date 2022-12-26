package com.microtimemanagement.apiservice.dto.request;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequestDTO {

    @NotNull
    @NotEmpty(message = "Username cannot not be empty.")
    private String username;

    @NotNull
    @NotEmpty(message = "Password cannot not be empty.")
    private String password;


    @NotNull
    @Email(message = ErrorConstants.EMAIL_NOT_VALID)
    @NotEmpty(message = "Email cannot not be empty.")
    private String email;

    @NotNull
    @NotEmpty(message = "First name cannot not be empty.")
    private String firstName;

    private String lastName;

}
