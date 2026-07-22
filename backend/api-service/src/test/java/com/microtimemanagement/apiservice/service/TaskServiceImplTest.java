package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.TaskDTO;
import com.microtimemanagement.apiservice.enums.TaskPriority;
import com.microtimemanagement.apiservice.enums.TaskStatus;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Task;
import com.microtimemanagement.apiservice.repository.TaskRepository;
import com.microtimemanagement.apiservice.service.impl.TaskServiceImpl;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Task Service Tests")
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final String UID = "owner-uid-1";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(currentUserProvider.currentUid()).thenReturn(UID);
    }

    @Test
    @DisplayName("create defaults status TODO / priority MEDIUM and stamps owner + links")
    void createDefaults() {
        Mockito.when(taskRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        TaskDTO result = taskService.create(
                TaskDTO.builder().name("Write spec").projectId("proj-1").build());

        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        Mockito.verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isEqualTo(UID);
        assertThat(captor.getValue().getProjectId()).isEqualTo("proj-1");
    }

    @Test
    @DisplayName("listByProject is scoped to the current user")
    void listByProjectScoped() {
        Mockito.when(taskRepository.findByProjectIdAndOwnerAndIsActiveTrueOrderByCreatedAtDesc("proj-1", UID))
                .thenReturn(List.of(Task.builder().id("t1").name("A").owner(UID).build()));

        assertThat(taskService.listByProject("proj-1")).extracting(TaskDTO::getId).containsExactly("t1");
    }

    @Test
    @DisplayName("listSubtasks is scoped to the current user")
    void listSubtasksScoped() {
        Mockito.when(taskRepository.findByParentTaskIdAndOwnerAndIsActiveTrueOrderByCreatedAtDesc("t1", UID))
                .thenReturn(List.of(Task.builder().id("t2").name("sub").parentTaskId("t1").owner(UID).build()));

        assertThat(taskService.listSubtasks("t1")).extracting(TaskDTO::getParentTaskId).containsExactly("t1");
    }

    @Test
    @DisplayName("getById throws Not Found for a task not owned by the current user")
    void getByIdNotFound() {
        Mockito.when(taskRepository.findByIdAndOwnerAndIsActiveTrue("nope", UID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> taskService.getById("nope"));
    }

    @Test
    @DisplayName("update patches only non-null fields")
    void updatePatches() {
        Task existing = Task.builder().id("t1").name("Old").status(TaskStatus.TODO)
                .priority(TaskPriority.LOW).owner(UID).build();
        Mockito.when(taskRepository.findByIdAndOwnerAndIsActiveTrue("t1", UID)).thenReturn(Optional.of(existing));
        Mockito.when(taskRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        TaskDTO result = taskService.update("t1",
                TaskDTO.builder().status(TaskStatus.DONE).build());

        assertThat(result.getName()).isEqualTo("Old");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.LOW);
    }
}
