package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.model.User;
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

    private RefreshTokenDTO refreshTokenDTO;

    private User user;

    private Date createdAt;

    private Date lastUpdatedAt;

    private Boolean isActive;


}
