package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.RoleConstants;
import com.microtimemanagement.apiservice.converter.RoleConverter;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.model.Role;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import com.microtimemanagement.apiservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    /**
     * @param roles
     * @return
     */

    private final RoleRepository roleRepository;
    private final RoleConverter roleConverter;

    @Override
    public Set<String> getRoleNamesForIds(Set<String> roles) {
        return roles.stream().map(
                rId -> roleRepository.findByIdAndIsActiveTrue(rId).getName()
        ).collect(Collectors.toSet());
    }

    /**
     * @param roleNames
     * @return
     */
    @Override
    public Set<RoleDTO> findActiveRolesByName(List<String> roleNames) {
        List<Role> roles = roleRepository.findByNameInAndIsActiveTrue(roleNames);
        log.info("Retrieved active roles by name: {}", roles);
        return roles.stream().map(roleConverter::toDTO).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDefaultUserRoleIds() {
        Set<RoleDTO> roleDTOS = findActiveRolesByName(
                List.of(
                        RoleConstants.USER_OPS_ROLE_WITH_PREFIX,
                        RoleConstants.ACTIVITY_CRUD_WITH_PREFIX
                )
        );
        log.info("Default Roles: {}", roleDTOS);
        return roleDTOS.stream()
                .map(RoleDTO::getId)
                .collect(Collectors.toSet());
    }
}
