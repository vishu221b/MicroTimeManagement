package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.constants.RoleConstants;
import com.microtimemanagement.apiservice.converter.RoleConverter;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.NewRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.RoleUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.response.UserRoleResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import com.microtimemanagement.apiservice.model.Role;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import com.microtimemanagement.apiservice.service.RoleService;
import com.microtimemanagement.apiservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final UserService userService;

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

    /**
     * TODO: Move Role addition and removal from User to Admin Service
     * @param requestDTO
     * @return
     */
    @Override
    public UserRoleResponseDTO addRoleToUser(UserRoleRequestDTO requestDTO) {
        Role role = findRoleByName(requestDTO.getRoleName());
        if(null!=role){
            User user = userService.getUserByUid(requestDTO.getUserUid());
            Set<String> roles= user.getRoles();
            roles.add(role.getId());
            user.setRoles(roles);
            return UserRoleResponseDTO.builder()
                    .user(userService.saveUser(user))
                    .message(ResponseMessages.ROLE_ASSIGNED_TO_USER_SUCCESSFULLY)
                    .build();
        }
        throw new MicroTimeManagementException(ErrorConstants.ROLE_NOT_FOUND_ERROR);
    }

    @Override
    public UserRoleResponseDTO removeRoleForUser(UserRoleRequestDTO requestDTO) {
        Role role = findRoleByName(requestDTO.getRoleName());
        if(null!=role){
            User user = userService.getUserByUid(requestDTO.getUserUid());
            user.getRoles().remove(role.getId());
            return UserRoleResponseDTO.builder()
                    .user(userService.saveUser(user))
                    .message(ResponseMessages.ROLE_REMOVED_FROM_USER_SUCCESSFULLY)
                    .build();
        }
        throw new MicroTimeManagementException(ErrorConstants.ROLE_NOT_FOUND_ERROR);

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
        return null;
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
        Page<Role> rolePage = roleRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.ASC, "_id"));
        return rolePage.get().map(roleConverter::toDTO).toList();
    }
}
