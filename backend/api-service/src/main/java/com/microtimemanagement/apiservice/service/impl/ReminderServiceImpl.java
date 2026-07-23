package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.ReminderDTO;
import com.microtimemanagement.apiservice.enums.ReminderStatus;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Reminder;
import com.microtimemanagement.apiservice.repository.ReminderRepository;
import com.microtimemanagement.apiservice.service.ReminderService;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ReminderDTO create(ReminderDTO dto) {
        Reminder reminder = Reminder.builder()
                .title(dto.getTitle())
                .notes(dto.getNotes())
                .remindAt(dto.getRemindAt())
                .status(dto.getStatus() == null ? ReminderStatus.PENDING : dto.getStatus())
                .emailReminder(Boolean.TRUE.equals(dto.getEmailReminder()))
                .linkedType(dto.getLinkedType())
                .linkedId(dto.getLinkedId())
                .owner(currentUserProvider.currentUid())
                .build();
        return toDTO(reminderRepository.save(reminder));
    }

    @Override
    public List<ReminderDTO> listForCurrentUser() {
        return reminderRepository
                .findActiveForOwner(currentUserProvider.currentUid())
                .stream().map(this::toDTO).toList();
    }

    @Override
    public ReminderDTO getById(String id) {
        return toDTO(loadOwned(id));
    }

    @Override
    public ReminderDTO update(String id, ReminderDTO dto) {
        Reminder r = loadOwned(id);
        if (dto.getTitle() != null) r.setTitle(dto.getTitle());
        if (dto.getNotes() != null) r.setNotes(dto.getNotes());
        if (dto.getRemindAt() != null) {
            r.setRemindAt(dto.getRemindAt());
            // Rescheduling clears the sent-marker so a moved reminder can email again.
            r.setEmailSentAt(null);
        }
        if (dto.getStatus() != null) r.setStatus(dto.getStatus());
        if (dto.getEmailReminder() != null) r.setEmailReminder(dto.getEmailReminder());
        if (dto.getLinkedType() != null) r.setLinkedType(dto.getLinkedType());
        if (dto.getLinkedId() != null) r.setLinkedId(dto.getLinkedId());
        return toDTO(reminderRepository.save(r));
    }

    @Override
    public void softDelete(String id) {
        Reminder r = loadOwned(id);
        r.setIsActive(Boolean.FALSE);
        reminderRepository.save(r);
    }

    private Reminder loadOwned(String id) {
        return reminderRepository
                .findByIdAndOwnerAndIsActiveTrue(id, currentUserProvider.currentUid())
                .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.REMINDER_NOT_FOUND));
    }

    private ReminderDTO toDTO(Reminder r) {
        return ReminderDTO.builder()
                .id(r.getId())
                .title(r.getTitle())
                .notes(r.getNotes())
                .remindAt(r.getRemindAt())
                .status(r.getStatus())
                .emailReminder(r.getEmailReminder())
                .linkedType(r.getLinkedType())
                .linkedId(r.getLinkedId())
                .createdAt(r.getCreatedAt())
                .lastUpdatedAt(r.getLastUpdatedAt())
                .isActive(r.getIsActive())
                .build();
    }
}
