package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiPathConstants;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.RoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.response.UserRoleResponseDTO;
import com.microtimemanagement.apiservice.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiPathConstants.ADMIN_BASE_V1)
public class AdminController {

    //TODO: Implement later once App is Live
    final AdminService adminService;

    @RequestMapping(value = ApiPathConstants.EMPTY_BASE, method = RequestMethod.POST)
    @ResponseBody
    public RoleDTO createNew(@RequestBody RoleRequestDTO requestDTO){
        return adminService.createNew(requestDTO);
    }

    @RequestMapping(value = ApiPathConstants.EMPTY_BASE, method = RequestMethod.DELETE)
    @ResponseBody
    public RoleDTO deleteRole(@RequestBody RoleRequestDTO requestDTO){
        return adminService.setInactive(requestDTO.getName());
    }

    @RequestMapping(value = ApiPathConstants.EMPTY_BASE, method = RequestMethod.GET)
    @ResponseBody
    public RoleDTO getRole(@RequestParam String roleName){
        return adminService.getDTOByName(roleName);
    }

    @RequestMapping(value = ApiPathConstants.ADD_ROLE_TO_USER, method = RequestMethod.POST)
    @ResponseBody
    public UserRoleResponseDTO addRoleToUser(@RequestBody UserRoleRequestDTO requestDTO){
        return adminService.addRoleToUser(requestDTO);
    }


}
