package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.NewRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.RoleUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.response.UserRoleResponseDTO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

public interface RoleService {

    Set<String> getRoleNamesForIds(Set<String> roles);

    Set<RoleDTO> findActiveRolesByName(List<String> roleNames);

    Set<String> getDefaultUserRoleIds();

    UserRoleResponseDTO addRoleToUser(UserRoleRequestDTO requestDTO);

    UserRoleResponseDTO removeRoleForUser(@Valid UserRoleRequestDTO requestDTO);

    RoleDTO createNewRole(@Valid NewRoleRequestDTO newRoleRequestDTO);

    RoleDTO updateRoleDetails(@Valid RoleUpdateRequestDTO roleUpdateRequestDTO);

    String deleteRole(String roleId);

    RoleDTO getRoleById(String roleId);

    List<RoleDTO> getAllRoles(Integer pageNumber, Integer pageSize);
}
