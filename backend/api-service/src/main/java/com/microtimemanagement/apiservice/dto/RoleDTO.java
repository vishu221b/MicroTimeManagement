package com.microtimemanagement.apiservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private String id;

    private String name;

    private Date createdAt;

    private Date lastUpdatedAt;

    private Boolean isActive;

}
