package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.TaskDTO;
import com.microtimemanagement.apiservice.enums.TaskPriority;
import com.microtimemanagement.apiservice.enums.TaskStatus;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Task;
import com.microtimemanagement.apiservice.repository.TaskRepository;
import com.microtimemanagement.apiservice.service.TaskService;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public TaskDTO create(TaskDTO dto) {
        Task task = Task.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .status(dto.getStatus() == null ? TaskStatus.TODO : dto.getStatus())
                .priority(dto.getPriority() == null ? TaskPriority.MEDIUM : dto.getPriority())
                .projectId(dto.getProjectId())
                .parentTaskId(dto.getParentTaskId())
                .owner(currentUserProvider.currentUid())
                .build();
        return toDTO(taskRepository.save(task));
    }

    @Override
    public List<TaskDTO> listForCurrentUser() {
        return taskRepository
                .findByOwnerAndIsActiveTrueOrderByCreatedAtDesc(currentUserProvider.currentUid())
                .stream().map(this::toDTO).toList();
    }

    @Override
    public List<TaskDTO> listByProject(String projectId) {
        return taskRepository
                .findByProjectIdAndOwnerAndIsActiveTrueOrderByCreatedAtDesc(projectId, currentUserProvider.currentUid())
                .stream().map(this::toDTO).toList();
    }

    @Override
    public List<TaskDTO> listSubtasks(String parentTaskId) {
        return taskRepository
                .findByParentTaskIdAndOwnerAndIsActiveTrueOrderByCreatedAtDesc(parentTaskId, currentUserProvider.currentUid())
                .stream().map(this::toDTO).toList();
    }

    @Override
    public TaskDTO getById(String id) {
        return toDTO(loadOwned(id));
    }

    @Override
    public TaskDTO update(String id, TaskDTO dto) {
        Task task = loadOwned(id);
        if (dto.getName() != null) task.setName(dto.getName());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getPriority() != null) task.setPriority(dto.getPriority());
        if (dto.getProjectId() != null) task.setProjectId(dto.getProjectId());
        if (dto.getParentTaskId() != null) task.setParentTaskId(dto.getParentTaskId());
        return toDTO(taskRepository.save(task));
    }

    @Override
    public void softDelete(String id) {
        Task task = loadOwned(id);
        task.setIsActive(Boolean.FALSE);
        taskRepository.save(task);
    }

    private Task loadOwned(String id) {
        return taskRepository
                .findByIdAndOwnerAndIsActiveTrue(id, currentUserProvider.currentUid())
                .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.TASK_NOT_FOUND));
    }

    private TaskDTO toDTO(Task t) {
        return TaskDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .dueDate(t.getDueDate())
                .status(t.getStatus())
                .priority(t.getPriority())
                .projectId(t.getProjectId())
                .parentTaskId(t.getParentTaskId())
                .createdAt(t.getCreatedAt())
                .lastUpdatedAt(t.getLastUpdatedAt())
                .isActive(t.getIsActive())
                .build();
    }
}
