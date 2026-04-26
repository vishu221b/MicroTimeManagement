package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.RoleDTO;

import java.util.List;
import java.util.Set;

public interface RoleService {

    Set<String> getRoleNamesForIds(Set<String> roles);

    Set<RoleDTO> findActiveRolesByName(List<String> roleNames);

    Set<String> getDefaultUserRoleIds();
}
