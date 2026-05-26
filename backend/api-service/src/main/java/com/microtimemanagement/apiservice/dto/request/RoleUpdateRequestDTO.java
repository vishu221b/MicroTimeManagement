package com.microtimemanagement.apiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleUpdateRequestDTO {

    @NotBlank(message = "Role id cannot be empty.")
    public String roleId;

    @NotBlank(message = "Role name cannot be empty.")
    public String roleName;

}
