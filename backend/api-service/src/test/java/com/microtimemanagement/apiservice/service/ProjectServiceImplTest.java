package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.ProjectDTO;
import com.microtimemanagement.apiservice.enums.ProjectStatus;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Project;
import com.microtimemanagement.apiservice.repository.ProjectRepository;
import com.microtimemanagement.apiservice.service.impl.ProjectServiceImpl;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Project Service Tests")
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    private static final String UID = "owner-uid-1";

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(currentUserProvider.currentUid()).thenReturn(UID);
    }

    @Test
    @DisplayName("create defaults status to ACTIVE and stamps the current user as owner")
    void createDefaultsStatusAndOwner() {
        Mockito.when(projectRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        ProjectDTO result = projectService.create(
                ProjectDTO.builder().name("Website").description("redesign").color("#10b981").build());

        assertThat(result.getName()).isEqualTo("Website");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        var captor = org.mockito.ArgumentCaptor.forClass(Project.class);
        Mockito.verify(projectRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isEqualTo(UID);
    }

    @Test
    @DisplayName("list returns only the current user's active projects")
    void listScopedToOwner() {
        Mockito.when(projectRepository.findActiveForOwner(UID))
                .thenReturn(List.of(
                        Project.builder().id("p1").name("A").owner(UID).build(),
                        Project.builder().id("p2").name("B").owner(UID).build()));

        List<ProjectDTO> result = projectService.listForCurrentUser();

        assertThat(result).extracting(ProjectDTO::getId).containsExactly("p1", "p2");
    }

    @Test
    @DisplayName("getById throws Not Found when the project is not owned by the current user")
    void getByIdNotFound() {
        Mockito.when(projectRepository.findByIdAndOwnerAndIsActiveTrue("nope", UID))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> projectService.getById("nope"));
    }

    @Test
    @DisplayName("update applies only the non-null fields")
    void updatePatchesFields() {
        Project existing = Project.builder().id("p1").name("Old").description("d").owner(UID)
                .status(ProjectStatus.ACTIVE).build();
        Mockito.when(projectRepository.findByIdAndOwnerAndIsActiveTrue("p1", UID))
                .thenReturn(Optional.of(existing));
        Mockito.when(projectRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        ProjectDTO result = projectService.update("p1",
                ProjectDTO.builder().name("New").status(ProjectStatus.ARCHIVED).build());

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("d");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
    }

    @Test
    @DisplayName("softDelete flips isActive to false")
    void softDelete() {
        Project existing = Project.builder().id("p1").name("A").owner(UID).isActive(Boolean.TRUE).build();
        Mockito.when(projectRepository.findByIdAndOwnerAndIsActiveTrue("p1", UID))
                .thenReturn(Optional.of(existing));

        projectService.softDelete("p1");

        assertThat(existing.getIsActive()).isFalse();
        Mockito.verify(projectRepository).save(existing);
    }
}
