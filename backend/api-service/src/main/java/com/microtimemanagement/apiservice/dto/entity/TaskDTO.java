package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.enums.TaskPriority;
import com.microtimemanagement.apiservice.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Request + response DTO for {@link com.microtimemanagement.apiservice.model.Task}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private String id;
    private String name;
    private String description;
    private String dueDate;
    private TaskStatus status;
    private TaskPriority priority;
    private String projectId;
    private String parentTaskId;
    private Date createdAt;
    private Date lastUpdatedAt;
    private Boolean isActive;
}
