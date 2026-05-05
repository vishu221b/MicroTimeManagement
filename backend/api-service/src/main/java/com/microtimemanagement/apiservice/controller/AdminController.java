package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.RoleRequestDTO;
import com.microtimemanagement.apiservice.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@Tag(name = "Admin", description = "Admin Operations")
@RequestMapping(ApiConstants.AdminEndpoint.API_BASE)
public class AdminController {

    final AdminService adminService;

    @RequestMapping(value = ApiConstants.EMPTY_BASE, method = RequestMethod.POST)
    @ResponseBody
    public RoleDTO createNew(@RequestBody RoleRequestDTO requestDTO){
        return adminService.createNew(requestDTO);
    }

    @RequestMapping(value = ApiConstants.EMPTY_BASE, method = RequestMethod.DELETE)
    @ResponseBody
    public RoleDTO deleteRole(@RequestBody RoleRequestDTO requestDTO){
        return adminService.setInactive(requestDTO.getName());
    }

    @RequestMapping(value = ApiConstants.EMPTY_BASE, method = RequestMethod.GET)
    @ResponseBody
    public RoleDTO getRole(@RequestParam String roleName){
        return adminService.getDTOByName(roleName);
    }





}
