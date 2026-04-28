package com.microtimemanagement.apiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequestDTO {

    @NotEmpty(message = "Role name cannot be empty.")
    private String roleName;

    @NotEmpty(message = "User Id cannot be empty.")
    private String userUid;

}
