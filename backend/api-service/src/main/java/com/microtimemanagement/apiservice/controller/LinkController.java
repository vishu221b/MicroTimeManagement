package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.dto.entity.LinkDTO;
import com.microtimemanagement.apiservice.enums.LinkableType;
import com.microtimemanagement.apiservice.service.LinkService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.LinkEndpoint.API_BASE)
@Tag(name = "Link", description = "Workflow links between projects/tasks/activities")
public class LinkController {

    private final LinkService linkService;

    @PostMapping
    public LinkDTO create(@RequestBody LinkDTO dto) {
        return linkService.create(dto);
    }

    /**
     * Lists the current user's links. Optionally filter to links originating
     * from a given entity via {@code sourceType} + {@code sourceId}.
     */
    @GetMapping
    public List<LinkDTO> list(
            @RequestParam(required = false) LinkableType sourceType,
            @RequestParam(required = false) String sourceId
    ) {
        if (sourceType != null && sourceId != null) {
            return linkService.listForEntity(sourceType, sourceId);
        }
        return linkService.listForCurrentUser();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        linkService.softDelete(id);
    }
}
