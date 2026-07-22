package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.dto.entity.TaskDTO;
import com.microtimemanagement.apiservice.service.TaskService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.TaskEndpoint.API_BASE)
@Tag(name = "Task", description = "Task management")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public TaskDTO create(@RequestBody TaskDTO dto) {
        return taskService.create(dto);
    }

    /**
     * Lists the current user's tasks. Optionally filter by {@code projectId}
     * (tasks in a project) or {@code parentTaskId} (sub-tasks of a task).
     */
    @GetMapping
    public List<TaskDTO> list(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String parentTaskId
    ) {
        if (projectId != null) return taskService.listByProject(projectId);
        if (parentTaskId != null) return taskService.listSubtasks(parentTaskId);
        return taskService.listForCurrentUser();
    }

    @GetMapping("/{id}")
    public TaskDTO get(@PathVariable String id) {
        return taskService.getById(id);
    }

    @PutMapping("/{id}")
    public TaskDTO update(@PathVariable String id, @RequestBody TaskDTO dto) {
        return taskService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        taskService.softDelete(id);
    }
}
