package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserDetailsUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UsersRolesUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationResultResponseDTO;
import com.microtimemanagement.apiservice.enums.UserRoleUpdateAction;
import com.microtimemanagement.apiservice.model.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface UserService extends UserDetailsService {

    User loadUserByUsername(String username) throws UsernameNotFoundException;

    NewUserResponseDTO createNewUser(NewUserRequestDTO requestDTO);

    UserDTO findDTOById(String userId);

    UserDTO findDTOByUsername(String username);

    UserDTO findDTOByEmail(String userEmail);

    UserDTO saveUserFromDTO(UserDTO userDTO, Boolean isUpdateRequest);

    UserDTO updateUserDetails(UserDetailsUpdateRequestDTO userDetailsUpdateRequestDTO);

    String deleteUserByUsername(String userId);

    String changeUserPassword(PasswordChangeRequestDTO passwordChangeRequestDTO);

    UserDTO getUserDTOByUid(String id);

    User getUserByUid(String id);

    UserDTO getUserProfile(User user);

    UserDTO loadUserDTOByUsername(String username);

    User replaceRoleIdsWithNamesForUser(User user);

    UserDTO saveUser(User user);

    PaginationResultResponseDTO<UserDTO> getAllUsers(PageRequest pageRequest);

    List<UserDTO> modifyUserRoles(@Valid UsersRolesUpdateRequestDTO usersRolesUpdateRequestDTO, UserRoleUpdateAction updateAction);
}
