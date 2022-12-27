package com.microtimemanagement.apiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
