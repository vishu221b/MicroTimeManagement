package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByOwnerAndIsActiveTrueOrderByCreatedAtDesc(String owner);

    List<Task> findByProjectIdAndOwnerAndIsActiveTrueOrderByCreatedAtDesc(String projectId, String owner);

    List<Task> findByParentTaskIdAndOwnerAndIsActiveTrueOrderByCreatedAtDesc(String parentTaskId, String owner);

    Optional<Task> findByIdAndOwnerAndIsActiveTrue(String id, String owner);
}
