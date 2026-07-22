package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findByOwnerAndIsActiveTrueOrderByCreatedAtDesc(String owner);

    Optional<Project> findByIdAndOwnerAndIsActiveTrue(String id, String owner);
}
