package com.microtimemanagement.apiservice.converter;

import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter implements BaseDTOConverter<Role, RoleDTO>{

    @Override
    public Role fromDTO(RoleDTO roleDTO) {
        return Role.builder()
                .name(roleDTO.getName())
                .build();
    }

    public RoleDTO toDTO(Role role){
        if(null!=role)
            return RoleDTO.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .createdAt(role.getCreatedAt())
                    .lastUpdatedAt(role.getLastUpdatedAt())
                    .isActive(role.getIsActive())
                    .build();
        return null;
    }

}
