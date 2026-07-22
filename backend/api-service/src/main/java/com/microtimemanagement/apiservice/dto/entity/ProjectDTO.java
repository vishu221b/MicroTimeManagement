package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Request + response DTO for {@link com.microtimemanagement.apiservice.model.Project}.
 * On create/update the audit fields are ignored (server-managed).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {

    private String id;
    private String name;
    private String description;
    private String color;
    private ProjectStatus status;
    private Date createdAt;
    private Date lastUpdatedAt;
    private Boolean isActive;
}
