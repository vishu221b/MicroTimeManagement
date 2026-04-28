package com.microtimemanagement.apiservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleUpdateRequestDTO {

    public String roleId;

    public String roleName;

}
