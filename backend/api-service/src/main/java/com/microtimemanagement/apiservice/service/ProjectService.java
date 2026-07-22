package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.ProjectDTO;

import java.util.List;

public interface ProjectService {

    ProjectDTO create(ProjectDTO dto);

    List<ProjectDTO> listForCurrentUser();

    ProjectDTO getById(String id);

    ProjectDTO update(String id, ProjectDTO dto);

    void softDelete(String id);
}
