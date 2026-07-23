package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.ProjectDTO;
import com.microtimemanagement.apiservice.enums.ProjectStatus;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Project;
import com.microtimemanagement.apiservice.repository.ProjectRepository;
import com.microtimemanagement.apiservice.service.ProjectService;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ProjectDTO create(ProjectDTO dto) {
        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .color(dto.getColor())
                .status(dto.getStatus() == null ? ProjectStatus.ACTIVE : dto.getStatus())
                .owner(currentUserProvider.currentUid())
                .build();
        return toDTO(projectRepository.save(project));
    }

    @Override
    public List<ProjectDTO> listForCurrentUser() {
        return projectRepository
                .findActiveForOwner(currentUserProvider.currentUid())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public ProjectDTO getById(String id) {
        return toDTO(loadOwned(id));
    }

    @Override
    public ProjectDTO update(String id, ProjectDTO dto) {
        Project project = loadOwned(id);
        if (dto.getName() != null) project.setName(dto.getName());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getColor() != null) project.setColor(dto.getColor());
        if (dto.getStatus() != null) project.setStatus(dto.getStatus());
        return toDTO(projectRepository.save(project));
    }

    @Override
    public void softDelete(String id) {
        Project project = loadOwned(id);
        project.setIsActive(Boolean.FALSE);
        projectRepository.save(project);
    }

    private Project loadOwned(String id) {
        return projectRepository
                .findByIdAndOwnerAndIsActiveTrue(id, currentUserProvider.currentUid())
                .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.PROJECT_NOT_FOUND));
    }

    private ProjectDTO toDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .color(p.getColor())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .lastUpdatedAt(p.getLastUpdatedAt())
                .isActive(p.getIsActive())
                .build();
    }
}
