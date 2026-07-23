package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.dto.entity.AttachmentDTO;
import com.microtimemanagement.apiservice.enums.AttachmentOwnerType;
import com.microtimemanagement.apiservice.service.AttachmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.AttachmentEndpoint.API_BASE)
@Tag(name = "Attachment", description = "Files/images attached to activities, tasks, projects, reminders")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public AttachmentDTO create(@Valid @RequestBody AttachmentDTO dto) {
        return attachmentService.create(dto);
    }

    @GetMapping
    public List<AttachmentDTO> list(
            @RequestParam AttachmentOwnerType parentType,
            @RequestParam String parentId
    ) {
        return attachmentService.listForParent(parentType, parentId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        attachmentService.softDelete(id);
    }
}
