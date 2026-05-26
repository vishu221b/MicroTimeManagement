package com.microtimemanagement.apiservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequestDTO {

    @NotBlank(message = "Old password cannot be empty.")
    private String oldPassword;

    @NotBlank(message = "New password cannot be empty.")
    @Size(min = 8, message = "New password must be at least 8 characters long.")
    private String newPassword;

    @JsonIgnore
    private String username;

}
