package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.dto.entity.ReminderDTO;
import com.microtimemanagement.apiservice.service.ReminderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.ReminderEndpoint.API_BASE)
@Tag(name = "Reminder", description = "Future reminders (in-app + optional email)")
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ReminderDTO create(@RequestBody ReminderDTO dto) {
        return reminderService.create(dto);
    }

    @GetMapping
    public List<ReminderDTO> list() {
        return reminderService.listForCurrentUser();
    }

    @GetMapping("/{id}")
    public ReminderDTO get(@PathVariable String id) {
        return reminderService.getById(id);
    }

    @PutMapping("/{id}")
    public ReminderDTO update(@PathVariable String id, @RequestBody ReminderDTO dto) {
        return reminderService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        reminderService.softDelete(id);
    }
}
