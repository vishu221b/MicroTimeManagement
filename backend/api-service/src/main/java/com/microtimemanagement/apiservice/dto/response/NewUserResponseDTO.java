package com.microtimemanagement.apiservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class NewUserResponseDTO {

    private String firstName;

    private String lastName;

    private String username;

    private String emailAddress;

    private Date createdAt;

}
