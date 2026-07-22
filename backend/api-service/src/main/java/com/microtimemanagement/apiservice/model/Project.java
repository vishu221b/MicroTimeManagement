package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.ProjectStatus;
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
 * A top-level container for tasks and activities. Owned by (scoped to) a single
 * user via {@code owner} (their uid).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_project")
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(length = 2000)
    private String description;

    // Hex accent color for the UI, e.g. "#10b981".
    private String color;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    // Owning user's uid.
    private String owner;
}
