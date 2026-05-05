package com.microtimemanagement.apiservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersRolesUpdateRequestDTO {

    @NotEmpty(message = "Role names cannot be empty.")
    private List<String> roleNames;

    private List<String> userIds;

    private List<String> usernames;

    private List<String> emails;

}
