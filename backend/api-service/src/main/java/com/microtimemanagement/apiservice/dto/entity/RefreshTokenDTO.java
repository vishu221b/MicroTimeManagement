package com.microtimemanagement.apiservice.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {

    private String id;

    private List<AccessTokenDTO> accessTokenDTOList;

    private AccessTokenDTO activeAccessTokenDTO;

    private String token;

    private Date expiresAt;

    private Date createdAt;

    private Date lastUpdatedAt;

    private Boolean isActive;

}
