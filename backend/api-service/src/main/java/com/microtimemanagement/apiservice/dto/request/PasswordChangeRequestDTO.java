package com.microtimemanagement.apiservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class PasswordChangeRequestDTO {

    private String oldPassword;

    private String newPassword;

    @JsonIgnore
    private String username;

}
