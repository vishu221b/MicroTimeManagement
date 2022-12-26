package com.microtimemanagement.apiservice.dto;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {

    private String id;

    private String token;

    private Date createdAt;

    private Date lastUpdatedAt;

    private Boolean isActive;


}
