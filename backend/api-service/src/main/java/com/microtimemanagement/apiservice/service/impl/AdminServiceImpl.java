package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.converter.RoleConverter;
import com.microtimemanagement.apiservice.dto.RoleDTO;
import com.microtimemanagement.apiservice.dto.UserDTO;
import com.microtimemanagement.apiservice.dto.response.UserRoleResponseDTO;
import com.microtimemanagement.apiservice.dto.request.RoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserRoleRequestDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import com.microtimemanagement.apiservice.model.Role;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import com.microtimemanagement.apiservice.service.AdminService;
import com.microtimemanagement.apiservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserService userService;

    private final RoleRepository roleRepository;


    private final RoleConverter roleConverter;

    @Override
    public RoleDTO createNew(RoleRequestDTO requestDTO) {
        Role role = getByName(requestDTO.getName());
        if(null!=role && role.getIsActive()){
            throw new MicroTimeManagementBadRequestException(ErrorConstants.ACTIVE_ROLE_ALREADY_EXISTS_ERROR);
        }
        if(requestDTO.getName().substring(0,4).equalsIgnoreCase("ROLE")){
            throw new MicroTimeManagementBadRequestException(ErrorConstants.ROLE_NAME_SHOULD_NOT_START_WITH_ROLE);
        }
        Role newRole = Role.builder().name(requestDTO.getName()).build();
        roleRepository.save(newRole);
        return RoleDTO.builder().
                id(newRole.getId())
                .name(newRole.getName())
                .createdAt(newRole.getCreatedAt())
                .lastUpdatedAt(newRole.getLastUpdatedAt())
                .isActive(newRole.getIsActive())
                .build();
    }

    @Override
    public RoleDTO getDTOByName(String roleName) {
        Role role = roleRepository.findByNameAndIsActiveTrue(roleName);
        if(null==role){
            log.error("Role {} not found.", roleName);
            throw new MicroTimeManagementBadRequestException(ErrorConstants.ROLE_NOT_FOUND_ERROR);
        }
        return RoleDTO.builder().
                id(role.getId())
                .name(role.getName())
                .createdAt(role.getCreatedAt())
                .lastUpdatedAt(role.getLastUpdatedAt())
                .isActive(role.getIsActive())
                .build();
    }

    @Override
    public Role getByName(String roleName) {
        return roleRepository.findByNameAndIsActiveTrue(roleName);
    }

    @Override
    public Role findById(String id) {
        Optional<Role> role = roleRepository.findById(id);
        if(role.isEmpty()){
            log.error("No role found for id: {}", id);
            throw new MicroTimeManagementBadRequestException(ErrorConstants.ROLE_NOT_FOUND_ERROR);
        }
        return role.get();
    }

    @Override
    public RoleDTO setInactive(String roleName) {
        Role role = roleRepository.findByNameAndIsActiveTrue(roleName);
        role.setIsActive(Boolean.FALSE);
        roleRepository.save(role);
        log.info("Setting role: {} as Inactive.", role.getName());
        return roleConverter.toDTO(role);
    }

    @Override
    public UserRoleResponseDTO addRoleToUser(UserRoleRequestDTO requestDTO) {
        Role role = getByName(requestDTO.getRoleName());
        if(null == role)
            throw new MicroTimeManagementBadRequestException(
                    String.format(ErrorConstants.ROLE_NOT_FOUND_WTH_NAME_ERROR, requestDTO.getRoleName()));
        UserDTO userDTO = null;
        String identifierValue = "";
        if(null!=requestDTO.getUserId()){
            userDTO = userService.findDTOById(requestDTO.getUserId());
            identifierValue = requestDTO.getUserId();
        } else if(null!=requestDTO.getUsername()){
            userDTO = userService.findDTOByUsername(requestDTO.getUsername());
            identifierValue = requestDTO.getUsername();
        } else if(null!=requestDTO.getUserEmail()){
            userDTO = userService.findDTOByEmail(requestDTO.getUserEmail());
            identifierValue = requestDTO.getUserEmail();
        }
        else{
            throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_USER_IDENTIFIER);
        }
        if(null == userDTO)
            throw new MicroTimeManagementBadRequestException(
                    String.format(ErrorConstants.USER_NOT_FOUND_FOR_IDENTIFIER,identifierValue));
        userDTO.getRoles().add(role.getId());
        userDTO.setRoles(userDTO.getRoles());
        Boolean isOk = userService.saveUserFromDTO(userDTO, Boolean.FALSE).getIsActive();
        if(isOk)
            return UserRoleResponseDTO.builder()
                    .user(userDTO)
                    .message(ResponseMessages.USER_ASSIGNED_WITH_ROLE_SUCCESS)
                    .build();
        else
            throw new MicroTimeManagementException(ErrorConstants.SOMETHING_WENT_WRONG);
    }

}
