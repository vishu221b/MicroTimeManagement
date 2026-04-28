package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserDetailsUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    User loadUserByUsername(String username) throws UsernameNotFoundException;

    GenericMessageResponseDTO<?> createNewUser(NewUserRequestDTO requestDTO);

    UserDTO findDTOById(String userId);

    UserDTO findDTOByUsername(String username);

    UserDTO findDTOByEmail(String userEmail);

    UserDTO saveUserFromDTO(UserDTO userDTO, Boolean isUpdateRequest);

    GenericMessageResponseDTO<?> updateUserDetails(UserDetailsUpdateRequestDTO userDetailsUpdateRequestDTO);

    GenericMessageResponseDTO<?> deleteUserByUsername(String userId);

    GenericMessageResponseDTO<?> changeUserPassword(PasswordChangeRequestDTO passwordChangeRequestDTO);

    UserDTO getUserDTOByUid(String id);

    User getUserByUid(String id);

    UserDTO getUserProfile(User user);

    UserDTO loadUserDTOByUsername(String username);

    User replaceRoleIdsWithNamesForUser(User user);

    UserDTO saveUser(User user);
}
