package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.TaskDTO;

import java.util.List;

public interface TaskService {

    TaskDTO create(TaskDTO dto);

    List<TaskDTO> listForCurrentUser();

    List<TaskDTO> listByProject(String projectId);

    List<TaskDTO> listSubtasks(String parentTaskId);

    TaskDTO getById(String id);

    TaskDTO update(String id, TaskDTO dto);

    void softDelete(String id);
}
