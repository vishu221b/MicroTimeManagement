package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.enums.ReminderStatus;
import com.microtimemanagement.apiservice.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, String> {

    List<Reminder> findByOwnerAndIsActiveTrueOrderByRemindAtAsc(String owner);

    Optional<Reminder> findByIdAndOwnerAndIsActiveTrue(String id, String owner);

    /**
     * Due, still-pending reminders that want an email and haven't been emailed
     * yet — the scheduler's work queue.
     */
    List<Reminder> findByIsActiveTrueAndEmailReminderTrueAndEmailSentAtIsNullAndStatusAndRemindAtLessThanEqual(
            ReminderStatus status, Long now);
}
