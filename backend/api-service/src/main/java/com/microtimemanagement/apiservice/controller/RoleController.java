package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.constants.PaginationConstants;
import com.microtimemanagement.apiservice.constants.RoleConstants;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.NewRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.RoleUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.service.RoleService;
import com.microtimemanagement.apiservice.utils.ApiUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.RoleEndpoint.API_BASE)
@Tag(name = "Roles", description = "Role Operations")
@Secured(value = { RoleConstants.ROLE_CRUD_WITH_PREFIX })
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @ResponseBody
    public GenericMessageResponseDTO<RoleDTO> createNewRole(
            @Valid @RequestBody NewRoleRequestDTO newRoleRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildSuccessResponseDTO(roleService.createNewRole(newRoleRequestDTO));
    }


    @PutMapping
    @ResponseBody
    public GenericMessageResponseDTO<RoleDTO> updateRole(
            @Valid @RequestBody RoleUpdateRequestDTO roleUpdateRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildSuccessResponseDTO(roleService.updateRoleDetails(roleUpdateRequestDTO));
    }

    @DeleteMapping
    @ResponseBody
    public GenericMessageResponseDTO<String> deleteRole(@RequestParam(name = "roleId") String roleId){
        return ApiUtils.buildSuccessResponseDTO(roleService.deleteRole(roleId));
    }

    @GetMapping
    @ResponseBody
    public GenericMessageResponseDTO<List<RoleDTO>> getAllRoles(
            @RequestParam(
                    defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER,
                    name = "page"
            ) Integer pageNumber,
            @RequestParam(
                    defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE,
                    name = "size"
            ) Integer pageSize,
            @RequestParam(name = "roleId", required = false) String roleId
    ){
        if(StringUtils.isNotEmpty(roleId))
            return ApiUtils.buildSuccessResponseDTO(List.of(roleService.getRoleById(roleId)));
        return ApiUtils.buildSuccessResponseDTO(roleService.getAllRoles(pageNumber, pageSize));
    }



}
