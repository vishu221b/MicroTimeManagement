package com.microtimemanagement.apiservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewRoleRequestDTO {

    @NotEmpty(message = "Role name cannot be empty.")
    private String roleName;

}
