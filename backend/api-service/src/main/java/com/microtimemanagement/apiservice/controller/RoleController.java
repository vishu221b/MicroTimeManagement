package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiPathConstants;
import com.microtimemanagement.apiservice.service.RoleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiPathConstants.ROLE_BASE_ENDPOINT)
@Tag(name = "Roles", description = "Role Operations")
public class RoleController {
    private final RoleService roleService;

}
