package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.converter.UserDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserDetailsUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementUserException;
import com.microtimemanagement.apiservice.factories.RoleTestFactory;
import com.microtimemanagement.apiservice.factories.UserTestFactory;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.UserRepository;
import com.microtimemanagement.apiservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author vishal.dogra
 * @since 1.0.0
 */

@DisplayName("User Service Tests")
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private RoleService roleService;

    @Spy
    private UserDTOConverter userDTOConverter;


    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should create a new user successfully.")
    void shouldCreateNewUser(){
        NewUserRequestDTO requestDTO = UserTestFactory.getNewUserRequestDTO();
        User user = UserTestFactory.existingAppUserEntity().build();
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        NewUserResponseDTO responseDTO = userService.createNewUser(requestDTO);
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getFirstName()).isEqualTo(requestDTO.getFirstName());
        assertThat(responseDTO.getLastName()).isEqualTo(requestDTO.getLastName());
        assertThat(responseDTO.getDateOfBirth()).isEqualTo(requestDTO.getDateOfBirth());
        assertThat(responseDTO.getEmailAddress()).isEqualTo(requestDTO.getEmail());
        assertThat(responseDTO.getUsername()).isEqualTo(requestDTO.getUsername());
    }

    @Test
    @DisplayName("Should throw UserException when another user with same username already exists while user registration.")
    void shouldThrowExceptionForExistingUserName_onNewUserCreationRequest(){
        NewUserRequestDTO requestDTO = UserTestFactory.getNewUserRequestDTO();
        Mockito.when(userRepository.findByUsername(requestDTO.getUsername()))
                .thenReturn(Optional.of(UserTestFactory.existingAppUserEntity().build()));
        assertThatExceptionOfType(MicroTimeManagementUserException.class).isThrownBy(() -> {
            userService.createNewUser(requestDTO);
        }).withMessage("User with same username already exists!");

    }

    @Test
    @DisplayName("Should throw UserException when another user with same email already exists while user registration.")
    void shouldThrowExceptionForExistingUserEmail_onNewUserCreationRequest(){
        NewUserRequestDTO requestDTO = UserTestFactory.getNewUserRequestDTO();
        Mockito.when(userRepository.findByEmail(requestDTO.getEmail()))
                .thenReturn(Optional.of(UserTestFactory.existingAppUserEntity().build()));
        assertThatExceptionOfType(MicroTimeManagementUserException.class).isThrownBy(() -> {
            userService.createNewUser(requestDTO);
        }).withMessage("User with same email already exists!");

    }

    @Test
    @DisplayName("Should fetch correct user using the username from repository.")
    void shouldLoadUserByUsername(){
        User user = UserTestFactory.existingAppUserEntity()
                .id(UUID.randomUUID().toString())
                .build();

        String username = UserTestFactory.MtmAppUserAttributes.USERNAME;

        Mockito.when(
                userRepository
                        .findByEmailOrUsernameAndIsActiveTrue(username, username)
                ).thenReturn(Optional.of(user));

        Mockito.when(
                roleService.getRoleNamesForIds(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_IDS)
        ).thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);

        User fetchedUser = userService.loadUserByUsername(UserTestFactory.MtmAppUserAttributes.USERNAME);

        assertThat(fetchedUser.getId()).isEqualTo(user.getId());
        assertThat(fetchedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(fetchedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(fetchedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(fetchedUser.getUid()).isEqualTo(user.getUid());
        assertThat(fetchedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(fetchedUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(fetchedUser.getDateOfBirth()).isEqualTo(user.getDateOfBirth());

        assertThat(fetchedUser.getRoles()).hasSize(2);
        assertThat(fetchedUser.getRoles())
                .contains(
                        RoleTestFactory.MtmRoleNames.USER_OPS,
                        RoleTestFactory.MtmRoleNames.ACTIVITY_OPS
                );
    }

    @Test
    @DisplayName("Should find user by userId and return UserDTO.")
    void shouldFindUserDTOByUserId(){
        String userId = UUID.randomUUID().toString();
        User appUser = UserTestFactory.existingAppUserEntity().id(userId).build();
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(appUser));

        UserDTO userDTO = userService.findDTOById(userId);

        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(appUser.getId());
    }

    @Test
    @DisplayName("Should find user by username and return UserDTO.")
    void shouldFindUserDTOByUsername(){
        String userId = UUID.randomUUID().toString();
        User appUser = UserTestFactory.existingAppUserEntity().id(userId).build();
        Mockito.when(
                roleService.getRoleNamesForIds(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_IDS)
        ).thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        Mockito.when(userRepository.findByUsernameAndIsActiveTrue(appUser.getUsername())).thenReturn(Optional.of(appUser));

        UserDTO userDTO = userService.findDTOByUsername(appUser.getUsername());

        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(appUser.getId());
        assertThat(userDTO.getUsername()).isEqualTo(appUser.getUsername());
        assertThat(userDTO.getEmail()).isEqualTo(appUser.getEmail());
        assertThat(userDTO.getFirstName()).isEqualTo(appUser.getFirstName());
        assertThat(userDTO.getLastName()).isEqualTo(appUser.getLastName());
        assertThat(userDTO.getDateOfBirth()).isEqualTo(appUser.getDateOfBirth());
        assertThat(userDTO.getIsActive()).isEqualTo(appUser.getIsActive());
        assertThat(userDTO.getUid()).isEqualTo(appUser.getUid());
        assertThat(userDTO.getRoles()).hasSize(2);
        assertThat(userDTO.getRoles())
                .contains(
                        RoleTestFactory.MtmRoleNames.USER_OPS,
                        RoleTestFactory.MtmRoleNames.ACTIVITY_OPS
                );
    }

    @Test
    @DisplayName("Should find user by email and return UserDTO.")
    void shouldFindUserDTOByEmail(){
        String userId = UUID.randomUUID().toString();
        User appUser = UserTestFactory.existingAppUserEntity().id(userId).build();
        Mockito.when(
                roleService.getRoleNamesForIds(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_IDS)
        ).thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        Mockito.when(userRepository.findByEmailAndIsActiveTrue(appUser.getEmail())).thenReturn(Optional.of(appUser));

        UserDTO userDTO = userService.findDTOByEmail(appUser.getEmail());

        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(appUser.getId());
        assertThat(userDTO.getUsername()).isEqualTo(appUser.getUsername());
        assertThat(userDTO.getEmail()).isEqualTo(appUser.getEmail());
        assertThat(userDTO.getFirstName()).isEqualTo(appUser.getFirstName());
        assertThat(userDTO.getLastName()).isEqualTo(appUser.getLastName());
        assertThat(userDTO.getDateOfBirth()).isEqualTo(appUser.getDateOfBirth());
        assertThat(userDTO.getIsActive()).isEqualTo(appUser.getIsActive());
        assertThat(userDTO.getUid()).isEqualTo(appUser.getUid());
        assertThat(userDTO.getRoles()).hasSize(2);
        assertThat(userDTO.getRoles())
                .contains(
                        RoleTestFactory.MtmRoleNames.USER_OPS,
                        RoleTestFactory.MtmRoleNames.ACTIVITY_OPS
                );
    }

    @Test
    @DisplayName("Should get user profile for currently signed in user successfully.")
    void shouldGetUserProfileForAuthenticatedUser(){
        User authenticatedUser = UserTestFactory.existingAppUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-",""))
                .build();

        UserDTO userDTO = userService.getUserProfile(authenticatedUser);

        assertThat(userDTO.getId()).isEqualTo(authenticatedUser.getId());
        assertThat(userDTO.getUid()).isEqualTo(authenticatedUser.getUid());
    }

    // ---- updateUserDetails ----

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User installAuthenticatedPrincipal() {
        User principal = UserTestFactory.existingAppUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of()
                )
        );
        return principal;
    }

    @Test
    @DisplayName("updateUserDetails should mutate the authenticated user's editable fields and persist.")
    void shouldUpdateAuthenticatedUserDetails() {
        User principal = installAuthenticatedPrincipal();
        Mockito.when(userRepository.findByUidAndIsActiveTrue(principal.getUid())).thenReturn(principal);
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDetailsUpdateRequestDTO update = UserDetailsUpdateRequestDTO.builder()
                .uid(principal.getUid())
                .username(principal.getUsername())
                .email("renamed@example.com")
                .firstName("Renamed")
                .lastName("Person")
                .dateOfBirth(principal.getDateOfBirth())
                .build();

        UserDTO result = userService.updateUserDetails(update);

        assertThat(result.getEmail()).isEqualTo("renamed@example.com");
        assertThat(result.getFirstName()).isEqualTo("Renamed");
        assertThat(result.getLastName()).isEqualTo("Person");
        Mockito.verify(userRepository).save(principal);
    }

    @Test
    @DisplayName("updateUserDetails should reject when the request targets a uid other than the authenticated user.")
    void shouldRejectUpdateForOtherUser() {
        User principal = installAuthenticatedPrincipal();

        UserDetailsUpdateRequestDTO update = UserDetailsUpdateRequestDTO.builder()
                .uid("some-other-uid")
                .username(principal.getUsername())
                .email(principal.getEmail())
                .firstName(principal.getFirstName())
                .lastName(principal.getLastName())
                .dateOfBirth(principal.getDateOfBirth())
                .build();

        assertThatExceptionOfType(MicroTimeManagementUserException.class)
                .isThrownBy(() -> userService.updateUserDetails(update));
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    @DisplayName("updateUserDetails should throw bad request when the user lookup returns nothing.")
    void shouldThrowBadRequestWhenUserMissing() {
        User principal = installAuthenticatedPrincipal();
        Mockito.when(userRepository.findByUidAndIsActiveTrue(principal.getUid())).thenReturn(null);

        UserDetailsUpdateRequestDTO update = UserDetailsUpdateRequestDTO.builder()
                .uid(principal.getUid())
                .username(principal.getUsername())
                .email(principal.getEmail())
                .firstName(principal.getFirstName())
                .lastName(principal.getLastName())
                .dateOfBirth(principal.getDateOfBirth())
                .build();

        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> userService.updateUserDetails(update));
    }

    // ---- changeUserPassword ----

    @Test
    @DisplayName("changeUserPassword should rehash the password and return success when the old password matches.")
    void shouldChangePasswordWhenOldMatches() {
        User principal = UserTestFactory.existingAppUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
        Mockito.when(userRepository.findByEmailOrUsernameAndIsActiveTrue(principal.getUsername(), principal.getUsername()))
                .thenReturn(Optional.of(principal));
        Mockito.when(roleService.getRoleNamesForIds(Mockito.anySet()))
                .thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        Mockito.when(bCryptPasswordEncoder.matches("oldPass", principal.getPassword())).thenReturn(true);
        Mockito.when(bCryptPasswordEncoder.encode("newPass")).thenReturn("encoded-new");

        PasswordChangeRequestDTO request = new PasswordChangeRequestDTO();
        request.setUsername(principal.getUsername());
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        String message = userService.changeUserPassword(request);

        assertThat(message).isEqualTo(ResponseMessages.PASSWORD_CHANGED_SUCCESSFULLY);
        assertThat(principal.getPassword()).isEqualTo("encoded-new");
    }

    @Test
    @DisplayName("changeUserPassword should throw bad request when the old password does not match.")
    void shouldThrowWhenOldPasswordWrong() {
        User principal = UserTestFactory.existingAppUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
        Mockito.when(userRepository.findByEmailOrUsernameAndIsActiveTrue(principal.getUsername(), principal.getUsername()))
                .thenReturn(Optional.of(principal));
        Mockito.when(roleService.getRoleNamesForIds(Mockito.anySet()))
                .thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        Mockito.when(bCryptPasswordEncoder.matches("wrong", principal.getPassword())).thenReturn(false);

        PasswordChangeRequestDTO request = new PasswordChangeRequestDTO();
        request.setUsername(principal.getUsername());
        request.setOldPassword("wrong");
        request.setNewPassword("newPass");

        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> userService.changeUserPassword(request));
    }
}
