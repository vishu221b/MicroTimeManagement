package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    User loadUserByUsername(String username) throws UsernameNotFoundException;

    NewUserResponseDTO createNewUser(NewUserRequestDTO requestDTO);

    UserDTO findDTOById(String userId);

    UserDTO findDTOByUsername(String username);

    UserDTO findDTOByEmail(String userEmail);

    UserDTO saveUserFromDTO(UserDTO userDTO, Boolean isUpdateRequest);

    UserDTO updateUserDetails(UserDTO userDTO);

    GenericMessageResponseDTO deleteUserByUsername(String userId);

    GenericMessageResponseDTO changeUserPassword(PasswordChangeRequestDTO passwordChangeRequestDTO);
}
