package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.RoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UsersRolesUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.UserRoleResponseDTO;
import com.microtimemanagement.apiservice.model.Role;

public interface AdminService {

    RoleDTO createNew(RoleRequestDTO requestDTO);

    RoleDTO getDTOByName(String roleName);

    Role getByName(String roleName);

    Role findById(String id);

    RoleDTO setInactive(String roleName);

}
