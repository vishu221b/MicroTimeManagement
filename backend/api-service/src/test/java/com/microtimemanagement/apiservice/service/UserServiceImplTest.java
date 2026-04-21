package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.TestDataFactory;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
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

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private RoleService roleService;


    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateNewUser(){
        NewUserRequestDTO requestDTO = TestDataFactory.getNewUserRequestDTO();
        GenericMessageResponseDTO<NewUserResponseDTO>
                responseDTO = (GenericMessageResponseDTO<NewUserResponseDTO>) userService.createNewUser(requestDTO);
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getPayload().getFirstName()).isEqualTo(requestDTO.getFirstName());
    }

    @Test
    void shouldThrowExceptionForExistingUserOnNewUserCreationRequest(){
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
    void shouldFindUserDTOByUserId(){}

    @Test
    void shouldFindUserDTOByUsername(){}

    @Test
    void shouldFindUserDTOByEmail(){}

    @Test
    void shouldSaveUserDetailsFromUserDTO(){}

    @Test
    void shouldUpdateUserDetails(){}

    @Test
    void shouldDeleteUserByUsername(){}

    @Test
    void shouldChangeUserPassword(){}

    @Test
    void shouldGetUserDTOByUserUID(){}

    @Test
    void shouldGetUserEntityByUserUID(){}

    @Test
    void shouldGetUserProfileForAuthenticatedUser(){}

    @Test
    void shouldLoadUserDTOByUsername(){}


}
