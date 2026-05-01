package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiPathConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.constants.RoleConstants;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.NewRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.RoleUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.dto.response.UserRoleResponseDTO;
import com.microtimemanagement.apiservice.service.RoleService;
import com.microtimemanagement.apiservice.utils.ApiUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiPathConstants.ROLE_BASE_ENDPOINT)
@Tag(name = "Roles", description = "Role Operations")
@Secured(value = { RoleConstants.ROLE_CRUD_WITH_PREFIX })
public class RoleController {
    private final RoleService roleService;

    // TODO: Move to ADMIN resource
    @RequestMapping(value = ApiPathConstants.ADD_ROLE_TO_USER, method = RequestMethod.POST)
    @ResponseBody
    public GenericMessageResponseDTO<?> addRoleToUser(
            @Valid @RequestBody UserRoleRequestDTO requestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildSuccessResponseDTO(
                roleService.addRoleToUser(requestDTO)
        );
    }

    //TODO: Move to ADMIN resource
    @RequestMapping(value = ApiPathConstants.REMOVE_ROLE_FOR_USER, method = RequestMethod.DELETE)
    public GenericMessageResponseDTO<UserRoleResponseDTO> removeRoleForUser(
            @Valid @RequestBody UserRoleRequestDTO requestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildSuccessResponseDTO(
                roleService.removeRoleForUser(requestDTO)
        );
    }

    @PostMapping
    public GenericMessageResponseDTO<RoleDTO> createNewRole(
            @Valid @RequestBody NewRoleRequestDTO newRoleRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildSuccessResponseDTO(roleService.createNewRole(newRoleRequestDTO));
    }


    @PutMapping
    public GenericMessageResponseDTO<RoleDTO> updateRole(
            @Valid @RequestBody RoleUpdateRequestDTO roleUpdateRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildSuccessResponseDTO(roleService.updateRoleDetails(roleUpdateRequestDTO));
    }

    @DeleteMapping
    public GenericMessageResponseDTO<String> deleteRole(@RequestParam(name = "roleId") String roleId){
        return ApiUtils.buildSuccessResponseDTO(roleService.deleteRole(roleId));
    }

    @GetMapping
    public GenericMessageResponseDTO<List<RoleDTO>> getAllRoles(
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(name = "roleId") String roleId
    ){
        if(!roleId.isEmpty())
            return ApiUtils.buildSuccessResponseDTO(List.of(roleService.getRoleById(roleId)));
        return ApiUtils.buildSuccessResponseDTO(roleService.getAllRoles(pageNumber, pageSize));
    }



}
