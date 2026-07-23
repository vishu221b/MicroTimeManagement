package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.dto.entity.TrashItemDTO;
import com.microtimemanagement.apiservice.enums.TrashItemType;
import com.microtimemanagement.apiservice.service.TrashService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.TrashEndpoint.API_BASE)
@Tag(name = "Trash", description = "Archive & trash — view, restore, archive, and permanently delete")
public class TrashController {

    private final TrashService trashService;

    /** state=DELETED (default) → soft-deleted items; state=ARCHIVED → archived items. */
    @GetMapping
    public List<TrashItemDTO> list(@RequestParam(defaultValue = "DELETED") String state) {
        return "ARCHIVED".equalsIgnoreCase(state)
                ? trashService.listArchived()
                : trashService.listDeleted();
    }

    @PutMapping("/archive")
    public void archive(@RequestParam TrashItemType type, @RequestParam String id) {
        trashService.archive(type, id);
    }

    @PutMapping("/restore")
    public void restore(@RequestParam TrashItemType type, @RequestParam String id) {
        trashService.restore(type, id);
    }

    @DeleteMapping
    public void purge(@RequestParam TrashItemType type, @RequestParam String id) {
        trashService.purge(type, id);
    }
}
