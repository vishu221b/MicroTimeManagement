package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.converter.UserDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementUserException;
import com.microtimemanagement.apiservice.factories.RoleTestFactory;
import com.microtimemanagement.apiservice.factories.UserTestFactory;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.UserRepository;
import com.microtimemanagement.apiservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private RoleService roleService;
    @Mock
    private UserDTOConverter userDTOConverter;


    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateNewUser(){
        NewUserRequestDTO requestDTO = UserTestFactory.getNewUserRequestDTO();
        GenericMessageResponseDTO<NewUserResponseDTO>
                responseDTO = (GenericMessageResponseDTO<NewUserResponseDTO>) userService.createNewUser(requestDTO);
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getPayload().getFirstName()).isEqualTo(requestDTO.getFirstName());
        assertThat(responseDTO.getPayload().getLastName()).isEqualTo(requestDTO.getLastName());
        assertThat(responseDTO.getPayload().getDateOfBirth()).isEqualTo(requestDTO.getDateOfBirth());
        assertThat(responseDTO.getPayload().getEmailAddress()).isEqualTo(requestDTO.getEmail());
        assertThat(responseDTO.getPayload().getUsername()).isEqualTo(requestDTO.getUsername());
    }

    @Test
    void shouldThrowExceptionForExistingUserName_onNewUserCreationRequest(){
        NewUserRequestDTO requestDTO = UserTestFactory.getNewUserRequestDTO();
        Mockito.when(userRepository.findByUsername(requestDTO.getUsername()))
                .thenReturn(Optional.of(UserTestFactory.existingAppUserEntity().build()));
        assertThatExceptionOfType(MicroTimeManagementUserException.class).isThrownBy(() -> {
            userService.createNewUser(requestDTO);
        }).withMessage("User with same username already exists!");

    }

    @Test
    void shouldThrowExceptionForExistingUserEmail_onNewUserCreationRequest(){
        NewUserRequestDTO requestDTO = UserTestFactory.getNewUserRequestDTO();
        Mockito.when(userRepository.findByEmail(requestDTO.getEmail()))
                .thenReturn(Optional.of(UserTestFactory.existingAppUserEntity().build()));
        assertThatExceptionOfType(MicroTimeManagementUserException.class).isThrownBy(() -> {
            userService.createNewUser(requestDTO);
        }).withMessage("User with same email already exists!");

    }

    @Test
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
    void shouldFindUserDTOByUserId(){
        String userId = UUID.randomUUID().toString();
        User appUser = UserTestFactory.existingAppUserEntity().id(userId).build();
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(appUser));
        Mockito.when(userDTOConverter.toDTO(appUser))
                .thenReturn(UserTestFactory.existingAppUserDTO().id(userId).build());

        UserDTO userDTO = userService.findDTOById(userId);

        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(appUser.getId());
    }

    @Test
    void shouldFindUserDTOByUsername(){
        String userId = UUID.randomUUID().toString();
        User appUser = UserTestFactory.existingAppUserEntity().id(userId).build();
        Mockito.when(
                roleService.getRoleNamesForIds(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_IDS)
        ).thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        Mockito.when(userRepository.findByUsernameAndIsActiveTrue(appUser.getUsername())).thenReturn(Optional.of(appUser));
        Mockito.when(userDTOConverter.toDTO(appUser))
                .thenReturn(UserTestFactory.existingAppUserDTO()
                        .uid(appUser.getUid())
                        .id(userId).build());

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
    void shouldFindUserDTOByEmail(){
        String userId = UUID.randomUUID().toString();
        User appUser = UserTestFactory.existingAppUserEntity().id(userId).build();
        Mockito.when(
                roleService.getRoleNamesForIds(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_IDS)
        ).thenReturn(UserTestFactory.MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES);
        Mockito.when(userRepository.findByEmailAndIsActiveTrue(appUser.getEmail())).thenReturn(Optional.of(appUser));
        Mockito.when(userDTOConverter.toDTO(appUser))
                .thenReturn(
                        UserTestFactory.existingAppUserDTO()
                        .id(userId).uid(appUser.getUid())
                                .build());

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
    void shouldGetUserProfileForAuthenticatedUser(){}


}
