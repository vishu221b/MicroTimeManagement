package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.dto.entity.ProjectDTO;
import com.microtimemanagement.apiservice.service.ProjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.ProjectEndpoint.API_BASE)
@Tag(name = "Project", description = "Project management")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ProjectDTO create(@RequestBody ProjectDTO dto) {
        return projectService.create(dto);
    }

    @GetMapping
    public List<ProjectDTO> list() {
        return projectService.listForCurrentUser();
    }

    @GetMapping("/{id}")
    public ProjectDTO get(@PathVariable String id) {
        return projectService.getById(id);
    }

    @PutMapping("/{id}")
    public ProjectDTO update(@PathVariable String id, @RequestBody ProjectDTO dto) {
        return projectService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        projectService.softDelete(id);
    }
}
