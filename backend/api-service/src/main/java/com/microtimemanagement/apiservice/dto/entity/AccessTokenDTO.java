package com.microtimemanagement.apiservice.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenDTO {

    private String id;

    private String token;

    private Date expiresAt;

    private Date createdAt;

    private Date lastUpdatedAt;

    private Boolean isActive;

}
