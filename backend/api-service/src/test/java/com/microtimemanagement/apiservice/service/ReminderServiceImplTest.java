package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.ReminderDTO;
import com.microtimemanagement.apiservice.enums.ReminderStatus;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Reminder;
import com.microtimemanagement.apiservice.repository.ReminderRepository;
import com.microtimemanagement.apiservice.service.impl.ReminderServiceImpl;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Reminder Service Tests")
@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    private static final String UID = "owner-uid-1";

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ReminderServiceImpl reminderService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(currentUserProvider.currentUid()).thenReturn(UID);
    }

    @Test
    @DisplayName("create defaults status PENDING, stamps owner, and coerces emailReminder")
    void createDefaults() {
        Mockito.when(reminderRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        ReminderDTO result = reminderService.create(
                ReminderDTO.builder().title("Dentist").remindAt(123L).build());

        assertThat(result.getStatus()).isEqualTo(ReminderStatus.PENDING);
        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        Mockito.verify(reminderRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isEqualTo(UID);
        assertThat(captor.getValue().getEmailReminder()).isFalse();
    }

    @Test
    @DisplayName("list is scoped to the current user, ordered by remindAt")
    void listScoped() {
        Mockito.when(reminderRepository.findActiveForOwner(UID))
                .thenReturn(List.of(Reminder.builder().id("r1").title("A").owner(UID).build()));

        assertThat(reminderService.listForCurrentUser()).extracting(ReminderDTO::getId).containsExactly("r1");
    }

    @Test
    @DisplayName("getById throws Not Found for a reminder not owned by the current user")
    void getByIdNotFound() {
        Mockito.when(reminderRepository.findByIdAndOwnerAndIsActiveTrue("nope", UID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> reminderService.getById("nope"));
    }

    @Test
    @DisplayName("rescheduling (new remindAt) clears the email-sent marker so it can fire again")
    void rescheduleClearsSentMarker() {
        Reminder existing = Reminder.builder().id("r1").title("A").remindAt(100L)
                .emailReminder(Boolean.TRUE).emailSentAt(new Date()).owner(UID).build();
        Mockito.when(reminderRepository.findByIdAndOwnerAndIsActiveTrue("r1", UID)).thenReturn(Optional.of(existing));
        Mockito.when(reminderRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        reminderService.update("r1", ReminderDTO.builder().remindAt(200L).build());

        assertThat(existing.getRemindAt()).isEqualTo(200L);
        assertThat(existing.getEmailSentAt()).isNull();
    }

    @Test
    @DisplayName("softDelete flips isActive to false")
    void softDelete() {
        Reminder existing = Reminder.builder().id("r1").title("A").owner(UID).isActive(Boolean.TRUE).build();
        Mockito.when(reminderRepository.findByIdAndOwnerAndIsActiveTrue("r1", UID)).thenReturn(Optional.of(existing));

        reminderService.softDelete("r1");

        assertThat(existing.getIsActive()).isFalse();
        Mockito.verify(reminderRepository).save(existing);
    }
}
