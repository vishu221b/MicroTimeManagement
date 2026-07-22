package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.LinkType;
import com.microtimemanagement.apiservice.enums.LinkableType;
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
 * A typed, directional link between any two linkable entities (project / task /
 * activity), letting a user wire them into a custom story or routine workflow.
 * Scoped to a single user via {@code owner} (their uid).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_entity_link")
@EqualsAndHashCode(callSuper = true)
public class EntityLink extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private LinkableType sourceType;

    private String sourceId;

    @Enumerated(EnumType.STRING)
    private LinkableType targetType;

    private String targetId;

    @Enumerated(EnumType.STRING)
    private LinkType linkType;

    // Owning user's uid.
    private String owner;
}
