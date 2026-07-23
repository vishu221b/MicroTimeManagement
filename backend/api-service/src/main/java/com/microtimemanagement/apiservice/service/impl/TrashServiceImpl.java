package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.TrashItemDTO;
import com.microtimemanagement.apiservice.enums.TrashItemType;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.BaseModel;
import com.microtimemanagement.apiservice.model.Project;
import com.microtimemanagement.apiservice.model.Reminder;
import com.microtimemanagement.apiservice.model.Task;
import com.microtimemanagement.apiservice.repository.ProjectRepository;
import com.microtimemanagement.apiservice.repository.ReminderRepository;
import com.microtimemanagement.apiservice.repository.TaskRepository;
import com.microtimemanagement.apiservice.service.TrashService;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashServiceImpl implements TrashService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ReminderRepository reminderRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<TrashItemDTO> listDeleted() {
        String uid = currentUserProvider.currentUid();
        List<TrashItemDTO> items = new ArrayList<>();
        projectRepository.findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(uid)
                .forEach(p -> items.add(fromProject(p)));
        taskRepository.findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(uid)
                .forEach(t -> items.add(fromTask(t)));
        reminderRepository.findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(uid)
                .forEach(r -> items.add(fromReminder(r)));
        return sorted(items);
    }

    @Override
    public List<TrashItemDTO> listArchived() {
        String uid = currentUserProvider.currentUid();
        List<TrashItemDTO> items = new ArrayList<>();
        projectRepository.findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(uid)
                .forEach(p -> items.add(fromProject(p)));
        taskRepository.findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(uid)
                .forEach(t -> items.add(fromTask(t)));
        reminderRepository.findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(uid)
                .forEach(r -> items.add(fromReminder(r)));
        return sorted(items);
    }

    @Override
    public void archive(TrashItemType type, String id) {
        BaseModel entity = loadOwned(type, id);
        entity.setArchived(Boolean.TRUE);
        save(type, entity);
    }

    @Override
    public void restore(TrashItemType type, String id) {
        BaseModel entity = loadOwned(type, id);
        entity.setIsActive(Boolean.TRUE);
        entity.setArchived(Boolean.FALSE);
        save(type, entity);
    }

    @Override
    public void purge(TrashItemType type, String id) {
        BaseModel entity = loadOwned(type, id);
        switch (type) {
            case PROJECT -> projectRepository.delete((Project) entity);
            case TASK -> taskRepository.delete((Task) entity);
            case REMINDER -> reminderRepository.delete((Reminder) entity);
        }
    }

    private BaseModel loadOwned(TrashItemType type, String id) {
        String uid = currentUserProvider.currentUid();
        return switch (type) {
            case PROJECT -> projectRepository.findByIdAndOwner(id, uid)
                    .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.PROJECT_NOT_FOUND));
            case TASK -> taskRepository.findByIdAndOwner(id, uid)
                    .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.TASK_NOT_FOUND));
            case REMINDER -> reminderRepository.findByIdAndOwner(id, uid)
                    .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.REMINDER_NOT_FOUND));
        };
    }

    private void save(TrashItemType type, BaseModel entity) {
        switch (type) {
            case PROJECT -> projectRepository.save((Project) entity);
            case TASK -> taskRepository.save((Task) entity);
            case REMINDER -> reminderRepository.save((Reminder) entity);
        }
    }

    private List<TrashItemDTO> sorted(List<TrashItemDTO> items) {
        items.sort(Comparator.comparing(
                TrashItemDTO::getLastUpdatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return items;
    }

    private TrashItemDTO fromProject(Project p) {
        return TrashItemDTO.builder()
                .id(p.getId())
                .type(TrashItemType.PROJECT)
                .title(p.getName())
                .subtitle(p.getDescription())
                .archived(Boolean.TRUE.equals(p.getArchived()))
                .lastUpdatedAt(p.getLastUpdatedAt())
                .build();
    }

    private TrashItemDTO fromTask(Task t) {
        String subtitle = t.getStatus() == null ? null : t.getStatus().name().replace('_', ' ');
        return TrashItemDTO.builder()
                .id(t.getId())
                .type(TrashItemType.TASK)
                .title(t.getName())
                .subtitle(subtitle)
                .archived(Boolean.TRUE.equals(t.getArchived()))
                .lastUpdatedAt(t.getLastUpdatedAt())
                .build();
    }

    private TrashItemDTO fromReminder(Reminder r) {
        String subtitle = r.getRemindAt() == null ? null : new Date(r.getRemindAt()).toString();
        return TrashItemDTO.builder()
                .id(r.getId())
                .type(TrashItemType.REMINDER)
                .title(r.getTitle())
                .subtitle(subtitle)
                .archived(Boolean.TRUE.equals(r.getArchived()))
                .lastUpdatedAt(r.getLastUpdatedAt())
                .build();
    }
}
