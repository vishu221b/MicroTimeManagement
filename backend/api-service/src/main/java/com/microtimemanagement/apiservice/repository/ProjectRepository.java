package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    // Primary list: active and not archived (null archived treated as not-archived
    // so pre-archive rows still show).
    @Query("SELECT p FROM Project p WHERE p.owner = :owner AND p.isActive = true "
            + "AND (p.archived IS NULL OR p.archived = false) ORDER BY p.createdAt DESC")
    List<Project> findActiveForOwner(@Param("owner") String owner);

    // Archive view: active but archived.
    List<Project> findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(String owner);

    // Trash view: soft-deleted.
    List<Project> findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(String owner);

    Optional<Project> findByIdAndOwnerAndIsActiveTrue(String id, String owner);

    // Load regardless of active/archived state — for restore / archive / purge.
    Optional<Project> findByIdAndOwner(String id, String owner);
}
