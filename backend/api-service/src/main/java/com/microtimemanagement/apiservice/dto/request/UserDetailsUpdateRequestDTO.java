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
public class UserDetailsUpdateRequestDTO {

    @NotNull
    @NotEmpty(message = "Username cannot be empty.")
    private String username;

    @NotNull
    @NotEmpty(message = "Uid cannot be empty.")
    private String uid;

    @NotNull
    @Email(message = ErrorConstants.EMAIL_NOT_VALID)
    @NotEmpty(message = "Email cannot be empty.")
    private String email;

    @NotNull
    @NotEmpty(message = "First name cannot be empty.")
    private String firstName;

    @NotNull
    @NotEmpty(message = "Date of Birth cannot be empty.")
    private String dateOfBirth;

    private String lastName;

}
