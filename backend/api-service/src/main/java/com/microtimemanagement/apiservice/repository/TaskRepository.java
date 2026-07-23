package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    // Primary lists exclude archived (null archived treated as not-archived).
    @Query("SELECT t FROM Task t WHERE t.owner = :owner AND t.isActive = true "
            + "AND (t.archived IS NULL OR t.archived = false) ORDER BY t.createdAt DESC")
    List<Task> findActiveForOwner(@Param("owner") String owner);

    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.owner = :owner AND t.isActive = true "
            + "AND (t.archived IS NULL OR t.archived = false) ORDER BY t.createdAt DESC")
    List<Task> findActiveByProject(@Param("projectId") String projectId, @Param("owner") String owner);

    @Query("SELECT t FROM Task t WHERE t.parentTaskId = :parentTaskId AND t.owner = :owner AND t.isActive = true "
            + "AND (t.archived IS NULL OR t.archived = false) ORDER BY t.createdAt DESC")
    List<Task> findActiveSubtasks(@Param("parentTaskId") String parentTaskId, @Param("owner") String owner);

    // Archive view: active but archived.
    List<Task> findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(String owner);

    // Trash view: soft-deleted.
    List<Task> findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(String owner);

    Optional<Task> findByIdAndOwnerAndIsActiveTrue(String id, String owner);

    // Load regardless of active/archived state — for restore / archive / purge.
    Optional<Task> findByIdAndOwner(String id, String owner);
}
