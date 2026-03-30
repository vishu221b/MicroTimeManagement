package com.microtimemanagement.apiservice.dto.response;

import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponseDTO {

    private String message;

    private UserDTO user;

}
