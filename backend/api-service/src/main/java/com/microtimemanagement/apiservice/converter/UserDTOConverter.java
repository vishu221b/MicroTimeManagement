package com.microtimemanagement.apiservice.converter;

import com.microtimemanagement.apiservice.dto.UserDTO;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserDTOConverter implements BaseDTOConverter<User, UserDTO> {

    private final RoleRepository roleRepository;

    @Override
    public User fromDTO(UserDTO userDTO) {
        return User.builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .dateOfBirth(userDTO.getDateOfBirth())
                .roles(userDTO.getRoles().stream()
                        .map(r -> roleRepository.findByName(r).getId())
                                .collect(Collectors.toSet())
                )
                .build();
    }

    @Override
    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .id(user.getId())
                .uid(user.getUid())
                .createdAt(user.getCreatedAt())
                .isActive(user.getIsActive())
                .lastUpdatedAt(user.getLastUpdatedAt())
                .password(user.getPassword())
                .dateOfBirth(user.getDateOfBirth())
                .roles(user.getRoles().stream()
                        .map(r -> roleRepository.findById(r).get().getName())
                        .collect(Collectors.toSet())
                )
                .build();
    }

}
