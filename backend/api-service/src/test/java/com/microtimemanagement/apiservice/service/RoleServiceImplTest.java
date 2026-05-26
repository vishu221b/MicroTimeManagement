package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.converter.RoleConverter;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.request.NewRoleRequestDTO;
import com.microtimemanagement.apiservice.dto.request.RoleUpdateRequestDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Role;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import com.microtimemanagement.apiservice.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Role Service Tests")
@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Spy
    private RoleConverter roleConverter;

    @InjectMocks
    private RoleServiceImpl roleService;

    // -- createNewRole -----------------------------------------------------

    @Test
    @DisplayName("createNewRole should persist a role with the supplied name and return its DTO.")
    void shouldCreateNewRole() {
        NewRoleRequestDTO request = NewRoleRequestDTO.builder().roleName("MTM_NEW_ROLE").build();
        Mockito.when(roleRepository.save(Mockito.any(Role.class)))
                .thenAnswer(invocation -> {
                    Role role = invocation.getArgument(0);
                    role.setId("generated-id");
                    return role;
                });

        RoleDTO created = roleService.createNewRole(request);

        ArgumentCaptor<Role> saved = ArgumentCaptor.forClass(Role.class);
        Mockito.verify(roleRepository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("MTM_NEW_ROLE");
        assertThat(created.getName()).isEqualTo("MTM_NEW_ROLE");
        assertThat(created.getId()).isEqualTo("generated-id");
    }

    // -- getRoleById -------------------------------------------------------

    @Test
    @DisplayName("getRoleById should return the role DTO for an existing id.")
    void shouldGetRoleById() {
        Role role = Role.builder().id("role-id").name("MTM_USER_OPS").build();
        role.setIsActive(Boolean.TRUE);
        Mockito.when(roleRepository.findByIdAndIsActiveTrue("role-id")).thenReturn(role);

        RoleDTO dto = roleService.getRoleById("role-id");

        assertThat(dto.getId()).isEqualTo("role-id");
        assertThat(dto.getName()).isEqualTo("MTM_USER_OPS");
    }

    // -- getAllRoles -------------------------------------------------------

    @Test
    @DisplayName("getAllRoles should return a list mapped from a Page of roles.")
    void shouldListAllRoles() {
        Role first = Role.builder().id("1").name("MTM_ROLE_CRUD").build();
        Role second = Role.builder().id("2").name("MTM_USER_OPS").build();
        first.setIsActive(Boolean.TRUE);
        second.setIsActive(Boolean.TRUE);
        Page<Role> page = new PageImpl<>(List.of(first, second));
        Mockito.when(roleRepository.findAll(Mockito.any(Pageable.class))).thenReturn(page);

        List<RoleDTO> roles = roleService.getAllRoles(0, 10);

        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(RoleDTO::getName)
                .containsExactlyInAnyOrder("MTM_ROLE_CRUD", "MTM_USER_OPS");
    }

    // -- deleteRole --------------------------------------------------------

    @Test
    @DisplayName("deleteRole should soft-delete the role by setting isActive to false.")
    void shouldSoftDeleteRole() {
        Role role = Role.builder().id("role-id").name("MTM_USER_OPS").build();
        role.setIsActive(Boolean.TRUE);
        Mockito.when(roleRepository.findByIdAndIsActiveTrue("role-id")).thenReturn(role);
        Mockito.when(roleRepository.save(role)).thenReturn(role);

        String message = roleService.deleteRole("role-id");

        assertThat(message).isEqualTo(ResponseMessages.SUCCESS);
        assertThat(role.getIsActive()).isFalse();
        Mockito.verify(roleRepository).save(role);
    }

    // -- updateRoleDetails -------------------------------------------------

    @Test
    @DisplayName("updateRoleDetails should rename the role when the new name is free and persist.")
    void shouldRenameRole() {
        Role role = Role.builder().id("role-id").name("OLD_NAME").build();
        role.setIsActive(Boolean.TRUE);
        Mockito.when(roleRepository.findByIdAndIsActiveTrue("role-id")).thenReturn(role);
        Mockito.when(roleRepository.findByNameAndIsActiveTrue("NEW_NAME")).thenReturn(null);
        Mockito.when(roleRepository.save(role)).thenReturn(role);

        RoleUpdateRequestDTO request = RoleUpdateRequestDTO.builder()
                .roleId("role-id")
                .roleName("NEW_NAME")
                .build();

        RoleDTO result = roleService.updateRoleDetails(request);

        assertThat(result.getName()).isEqualTo("NEW_NAME");
        assertThat(role.getName()).isEqualTo("NEW_NAME");
    }

    @Test
    @DisplayName("updateRoleDetails should no-op when the name has not changed.")
    void shouldNoOpWhenNameUnchanged() {
        Role role = Role.builder().id("role-id").name("MTM_USER_OPS").build();
        role.setIsActive(Boolean.TRUE);
        Mockito.when(roleRepository.findByIdAndIsActiveTrue("role-id")).thenReturn(role);

        RoleUpdateRequestDTO request = RoleUpdateRequestDTO.builder()
                .roleId("role-id")
                .roleName("MTM_USER_OPS")
                .build();

        RoleDTO result = roleService.updateRoleDetails(request);

        assertThat(result.getName()).isEqualTo("MTM_USER_OPS");
        Mockito.verify(roleRepository, Mockito.never()).save(Mockito.any(Role.class));
    }

    @Test
    @DisplayName("updateRoleDetails should throw bad request when another active role already owns the new name.")
    void shouldRejectWhenNameConflicts() {
        Role role = Role.builder().id("role-id").name("OLD_NAME").build();
        role.setIsActive(Boolean.TRUE);
        Role conflicting = Role.builder().id("other-id").name("NEW_NAME").build();
        conflicting.setIsActive(Boolean.TRUE);
        Mockito.when(roleRepository.findByIdAndIsActiveTrue("role-id")).thenReturn(role);
        Mockito.when(roleRepository.findByNameAndIsActiveTrue("NEW_NAME")).thenReturn(conflicting);

        RoleUpdateRequestDTO request = RoleUpdateRequestDTO.builder()
                .roleId("role-id")
                .roleName("NEW_NAME")
                .build();

        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> roleService.updateRoleDetails(request))
                .withMessage(ErrorConstants.ACTIVE_ROLE_ALREADY_EXISTS_ERROR);
        Mockito.verify(roleRepository, Mockito.never()).save(Mockito.any(Role.class));
    }

    @Test
    @DisplayName("updateRoleDetails should throw not found when the id does not match any active role.")
    void shouldThrowNotFoundOnUnknownId() {
        Mockito.when(roleRepository.findByIdAndIsActiveTrue("missing-id")).thenReturn(null);

        RoleUpdateRequestDTO request = RoleUpdateRequestDTO.builder()
                .roleId("missing-id")
                .roleName("ANY_NAME")
                .build();

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> roleService.updateRoleDetails(request))
                .withMessage(ErrorConstants.ROLE_NOT_FOUND_ERROR);
    }
}
