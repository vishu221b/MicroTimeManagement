package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.TaskPriority;
import com.microtimemanagement.apiservice.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A unit of work. May belong to a {@link Project} (via {@code projectId}) or
 * stand alone, and may nest under another task (via {@code parentTaskId}) to
 * form sub-tasks. Scoped to a single user via {@code owner} (their uid).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_task")
@EqualsAndHashCode(callSuper = true)
public class Task extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(length = 2000)
    private String description;

    // yyyy-MM-dd, consistent with the rest of the app's date storage.
    private String dueDate;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    // Optional parent project (nullable — a task can stand alone).
    private String projectId;

    // Optional parent task for sub-tasks (nullable).
    private String parentTaskId;

    // Owning user's uid.
    private String owner;
}
