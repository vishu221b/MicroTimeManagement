package com.microtimemanagement.apiservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequestDTO {

    private String roleName;

    private String userId;

    private String username;

    private String userEmail;

}
