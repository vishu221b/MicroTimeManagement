package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.converter.UserDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserDetailsUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UsersRolesUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationResultResponseDTO;
import com.microtimemanagement.apiservice.enums.UserRoleUpdateAction;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
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
import java.util.Set;
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

    // ---- getAllUsers ----

    @Test
    @DisplayName("getAllUsers should resolve role IDs to role names on every user in the page.")
    void shouldResolveRoleNamesInGetAllUsers() {
        User u1 = UserTestFactory.existingAppUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
        User u2 = UserTestFactory.adminUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
        org.springframework.data.domain.Page<User> page =
                new org.springframework.data.domain.PageImpl<>(
                        List.of(u1, u2),
                        org.springframework.data.domain.PageRequest.of(
                                0, 10, org.springframework.data.domain.Sort.by("username")),
                        2
                );
        Mockito.when(userRepository.findAll(Mockito.any(org.springframework.data.domain.PageRequest.class)))
                .thenReturn(page);
        Mockito.when(roleService.getRoleNamesForIds(u1.getRoles()))
                .thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        Mockito.when(roleService.getRoleNamesForIds(u2.getRoles()))
                .thenReturn(UserTestFactory.MtmAdminUserAttributes.DEFAULT_ADMIN_USER_ROLE_NAMES);

        PaginationResultResponseDTO<UserDTO> result = userService.getAllUsers(
                org.springframework.data.domain.PageRequest.of(0, 10,
                        org.springframework.data.domain.Sort.by("username")));

        assertThat(result.getPayload()).hasSize(2);
        assertThat(result.getPayload().get(0).getRoles())
                .containsExactlyInAnyOrderElementsOf(
                        UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        assertThat(result.getPayload().get(1).getRoles())
                .containsExactlyInAnyOrderElementsOf(
                        UserTestFactory.MtmAdminUserAttributes.DEFAULT_ADMIN_USER_ROLE_NAMES);
    }

    // ---- modifyUserRoles ----

    @Test
    @DisplayName("modifyUserRoles should add role IDs to users matched by username and return role names.")
    void shouldAddRolesToUsersByUsername() {
        User principal = UserTestFactory.existingAppUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
        Mockito.when(userRepository.findByUsernameInAndIsActiveTrue(List.of(principal.getUsername())))
                .thenReturn(List.of(principal));
        Mockito.when(roleService.findActiveRolesByName(List.of(RoleTestFactory.MtmRoleNames.ADMIN_OPS)))
                .thenReturn(Set.of(RoleDTO.builder()
                        .id(RoleTestFactory.MtmRoleIds.ADMIN_OPS)
                        .name(RoleTestFactory.MtmRoleNames.ADMIN_OPS)
                        .build()));
        // After save, the service runs role-id -> name resolution on the response.
        Mockito.when(roleService.getRoleNamesForIds(Mockito.anySet()))
                .thenReturn(Set.of(
                        RoleTestFactory.MtmRoleNames.USER_OPS,
                        RoleTestFactory.MtmRoleNames.ACTIVITY_OPS,
                        RoleTestFactory.MtmRoleNames.ADMIN_OPS));

        UsersRolesUpdateRequestDTO request = new UsersRolesUpdateRequestDTO();
        request.setRoleNames(List.of(RoleTestFactory.MtmRoleNames.ADMIN_OPS));
        request.setUsernames(List.of(principal.getUsername()));
        // userIds + emails intentionally left null to exercise null-safety.

        // Snapshot the role IDs at the moment saveAll is called — captors hold
        // references, and the response-path resolution mutates user.roles after
        // saveAll, so an ArgumentCaptor would observe the post-resolution state.
        java.util.concurrent.atomic.AtomicReference<Set<String>> savedRoleIds =
                new java.util.concurrent.atomic.AtomicReference<>();
        Mockito.when(userRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<User> savedArg = invocation.getArgument(0);
                    savedRoleIds.set(new java.util.LinkedHashSet<>(savedArg.get(0).getRoles()));
                    return savedArg;
                });

        List<UserDTO> result = userService.modifyUserRoles(request, UserRoleUpdateAction.ADD);

        assertThat(savedRoleIds.get()).contains(RoleTestFactory.MtmRoleIds.ADMIN_OPS);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoles())
                .contains(RoleTestFactory.MtmRoleNames.ADMIN_OPS);
    }

    @Test
    @DisplayName("modifyUserRoles should remove role IDs from matched users.")
    void shouldRemoveRolesFromUsers() {
        User principal = UserTestFactory.adminUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .roles(new java.util.LinkedHashSet<>(
                        UserTestFactory.MtmAdminUserAttributes.DEFAULT_ADMIN_USER_ROLE_IDS))
                .build();
        Mockito.when(userRepository.findByUsernameInAndIsActiveTrue(List.of(principal.getUsername())))
                .thenReturn(List.of(principal));
        Mockito.when(roleService.findActiveRolesByName(List.of(RoleTestFactory.MtmRoleNames.ADMIN_OPS)))
                .thenReturn(Set.of(RoleDTO.builder()
                        .id(RoleTestFactory.MtmRoleIds.ADMIN_OPS)
                        .name(RoleTestFactory.MtmRoleNames.ADMIN_OPS)
                        .build()));
        Mockito.when(userRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(roleService.getRoleNamesForIds(Mockito.anySet()))
                .thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);

        UsersRolesUpdateRequestDTO request = new UsersRolesUpdateRequestDTO();
        request.setRoleNames(List.of(RoleTestFactory.MtmRoleNames.ADMIN_OPS));
        request.setUsernames(List.of(principal.getUsername()));

        userService.modifyUserRoles(request, UserRoleUpdateAction.REMOVE);

        assertThat(principal.getRoles())
                .doesNotContain(RoleTestFactory.MtmRoleIds.ADMIN_OPS);
    }

    @Test
    @DisplayName("modifyUserRoles should throw not-found when no identifier matched any active user.")
    void shouldThrowNotFoundWhenNoUserMatchedForRoleUpdate() {
        Mockito.when(userRepository.findByUsernameInAndIsActiveTrue(List.of("nobody")))
                .thenReturn(List.of());

        UsersRolesUpdateRequestDTO request = new UsersRolesUpdateRequestDTO();
        request.setRoleNames(List.of(RoleTestFactory.MtmRoleNames.ADMIN_OPS));
        request.setUsernames(List.of("nobody"));

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> userService.modifyUserRoles(request, UserRoleUpdateAction.ADD));
        Mockito.verify(userRepository, Mockito.never()).saveAll(Mockito.anyList());
    }

    @Test
    @DisplayName("modifyUserRoles should throw not-found when none of the supplied role names exist.")
    void shouldThrowNotFoundWhenNoRoleResolves() {
        User principal = UserTestFactory.existingAppUserEntity()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
        Mockito.when(userRepository.findByUsernameInAndIsActiveTrue(List.of(principal.getUsername())))
                .thenReturn(List.of(principal));
        Mockito.when(roleService.findActiveRolesByName(List.of("MTM_NONEXISTENT")))
                .thenReturn(Set.of());

        UsersRolesUpdateRequestDTO request = new UsersRolesUpdateRequestDTO();
        request.setRoleNames(List.of("MTM_NONEXISTENT"));
        request.setUsernames(List.of(principal.getUsername()));

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> userService.modifyUserRoles(request, UserRoleUpdateAction.ADD));
        Mockito.verify(userRepository, Mockito.never()).saveAll(Mockito.anyList());
    }
}
