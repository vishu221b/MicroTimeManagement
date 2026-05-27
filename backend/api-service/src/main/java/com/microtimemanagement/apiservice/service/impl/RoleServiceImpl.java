package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.constants.RoleConstants;
import com.microtimemanagement.apiservice.converter.RoleConverter;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.NewRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.RoleUpdateRequestDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Role;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import com.microtimemanagement.apiservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        // Hot path: this runs on every authenticated request (via
        // UserServiceImpl.replaceRoleIdsWithNamesForUser). A single $in query
        // beats the per-id round-trip even with the typical 1-3 roles per user;
        // and any future RBAC explosion stays O(1) queries instead of O(N).
        if (roles == null || roles.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        return roleRepository.findByIdInAndIsActiveTrue(roles).stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
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

    private Role findRoleByName(String role){
        return roleRepository.findByNameAndIsActiveTrue(role);
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

    @Override
    public RoleDTO createNewRole(NewRoleRequestDTO newRoleRequestDTO) {
        return roleConverter.toDTO(saveRole(Role.builder()
                .name(newRoleRequestDTO.getRoleName())
                .build()));
    }

    /**
     * @param roleUpdateRequestDTO
     * @return
     */
    @Override
    public RoleDTO updateRoleDetails(RoleUpdateRequestDTO roleUpdateRequestDTO) {
        Role role = roleRepository.findByIdAndIsActiveTrue(roleUpdateRequestDTO.getRoleId());
        if (null == role) {
            throw new MicroTimeManagementNotFoundException(ErrorConstants.ROLE_NOT_FOUND_ERROR);
        }
        String newName = roleUpdateRequestDTO.getRoleName().trim();
        if (newName.equals(role.getName())) {
            return roleConverter.toDTO(role);
        }
        Role conflicting = roleRepository.findByNameAndIsActiveTrue(newName);
        if (null != conflicting && !conflicting.getId().equals(role.getId())) {
            throw new MicroTimeManagementBadRequestException(ErrorConstants.ACTIVE_ROLE_ALREADY_EXISTS_ERROR);
        }
        role.setName(newName);
        return roleConverter.toDTO(saveRole(role));
    }

    /**
     * @param roleId
     * @return
     */
    @Override
    public String deleteRole(String roleId) {
        Role role = roleRepository.findByIdAndIsActiveTrue(roleId);
        role.setIsActive(Boolean.FALSE);
        saveRole(role);
        return ResponseMessages.SUCCESS;
    }

    private Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    /**
     * @param roleId
     * @return
     */
    @Override
    public RoleDTO getRoleById(String roleId) {
        return roleConverter.toDTO(roleRepository.findByIdAndIsActiveTrue(roleId));
    }

    /**
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @Override
    public List<RoleDTO> getAllRoles(Integer pageNumber, Integer pageSize) {
        Page<Role> rolePage = roleRepository.findAll(
                PageRequest.of(pageNumber, pageSize, Sort.Direction.ASC, "name")
        );
        return rolePage.getContent().stream().map(roleConverter::toDTO).toList();
    }
}
