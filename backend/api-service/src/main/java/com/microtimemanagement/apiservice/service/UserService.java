package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Principal;

public interface UserService extends UserDetailsService {

    User loadUserByUsername(String username) throws UsernameNotFoundException;

    GenericMessageResponseDTO<?> createNewUser(NewUserRequestDTO requestDTO);

    UserDTO findDTOById(String userId);

    UserDTO findDTOByUsername(String username);

    UserDTO findDTOByEmail(String userEmail);

    UserDTO saveUserFromDTO(UserDTO userDTO, Boolean isUpdateRequest);

    GenericMessageResponseDTO<?> updateUserDetails(UserDTO userDTO);

    GenericMessageResponseDTO<?> deleteUserByUsername(String userId);

    GenericMessageResponseDTO<?> changeUserPassword(PasswordChangeRequestDTO passwordChangeRequestDTO);

    UserDTO getUserByUid(String id);

    UserDTO getUserProfile(User user);

    UserDTO loadUserDTOByUsername(String username);
}
