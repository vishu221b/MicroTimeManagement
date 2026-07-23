package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.TrashItemDTO;
import com.microtimemanagement.apiservice.enums.TrashItemType;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Project;
import com.microtimemanagement.apiservice.model.Reminder;
import com.microtimemanagement.apiservice.model.Task;
import com.microtimemanagement.apiservice.repository.ProjectRepository;
import com.microtimemanagement.apiservice.repository.ReminderRepository;
import com.microtimemanagement.apiservice.repository.TaskRepository;
import com.microtimemanagement.apiservice.service.impl.TrashServiceImpl;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Trash Service Tests")
@ExtendWith(MockitoExtension.class)
class TrashServiceImplTest {

    private static final String UID = "owner-uid-1";

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ReminderRepository reminderRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TrashServiceImpl trashService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(currentUserProvider.currentUid()).thenReturn(UID);
    }

    @Test
    @DisplayName("listDeleted flattens soft-deleted projects/tasks/reminders into typed rows")
    void listDeletedFlattens() {
        Project p = new Project();
        p.setId("p1");
        p.setName("Website");
        Task t = new Task();
        t.setId("t1");
        t.setName("Design");
        Reminder r = new Reminder();
        r.setId("r1");
        r.setTitle("Standup");

        Mockito.when(projectRepository.findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(UID)).thenReturn(List.of(p));
        Mockito.when(taskRepository.findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(UID)).thenReturn(List.of(t));
        Mockito.when(reminderRepository.findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(UID)).thenReturn(List.of(r));

        List<TrashItemDTO> items = trashService.listDeleted();

        assertThat(items).hasSize(3);
        assertThat(items).extracting(TrashItemDTO::getType)
                .containsExactlyInAnyOrder(TrashItemType.PROJECT, TrashItemType.TASK, TrashItemType.REMINDER);
        assertThat(items).extracting(TrashItemDTO::getTitle)
                .containsExactlyInAnyOrder("Website", "Design", "Standup");
    }

    @Test
    @DisplayName("listArchived pulls archived-but-active items from every repository")
    void listArchivedPullsArchived() {
        Mockito.when(projectRepository.findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(UID))
                .thenReturn(List.of());
        Mockito.when(taskRepository.findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(UID))
                .thenReturn(List.of());
        Reminder r = new Reminder();
        r.setId("r1");
        r.setTitle("Renew licence");
        r.setArchived(Boolean.TRUE);
        Mockito.when(reminderRepository.findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(UID))
                .thenReturn(List.of(r));

        List<TrashItemDTO> items = trashService.listArchived();

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getType()).isEqualTo(TrashItemType.REMINDER);
        assertThat(items.get(0).getArchived()).isTrue();
    }

    @Test
    @DisplayName("archive sets archived=true and persists via the matching repository")
    void archiveSetsFlag() {
        Task t = new Task();
        t.setId("t1");
        Mockito.when(taskRepository.findByIdAndOwner("t1", UID)).thenReturn(Optional.of(t));

        trashService.archive(TrashItemType.TASK, "t1");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        Mockito.verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getArchived()).isTrue();
    }

    @Test
    @DisplayName("restore reactivates and un-archives the item")
    void restoreReactivates() {
        Project p = new Project();
        p.setId("p1");
        p.setIsActive(Boolean.FALSE);
        p.setArchived(Boolean.TRUE);
        Mockito.when(projectRepository.findByIdAndOwner("p1", UID)).thenReturn(Optional.of(p));

        trashService.restore(TrashItemType.PROJECT, "p1");

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        Mockito.verify(projectRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isTrue();
        assertThat(captor.getValue().getArchived()).isFalse();
    }

    @Test
    @DisplayName("purge hard-deletes the entity from its repository")
    void purgeHardDeletes() {
        Reminder r = new Reminder();
        r.setId("r1");
        Mockito.when(reminderRepository.findByIdAndOwner("r1", UID)).thenReturn(Optional.of(r));

        trashService.purge(TrashItemType.REMINDER, "r1");

        Mockito.verify(reminderRepository).delete(r);
    }

    @Test
    @DisplayName("operating on someone else's / missing item throws NotFound")
    void missingThrows() {
        Mockito.when(taskRepository.findByIdAndOwner("nope", UID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> trashService.restore(TrashItemType.TASK, "nope"));
    }
}
