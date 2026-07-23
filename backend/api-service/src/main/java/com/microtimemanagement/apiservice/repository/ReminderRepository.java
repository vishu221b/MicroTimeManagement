package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.enums.ReminderStatus;
import com.microtimemanagement.apiservice.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, String> {

    // Primary list: active and not archived (null archived treated as not-archived).
    @Query("SELECT r FROM Reminder r WHERE r.owner = :owner AND r.isActive = true "
            + "AND (r.archived IS NULL OR r.archived = false) ORDER BY r.remindAt ASC")
    List<Reminder> findActiveForOwner(@Param("owner") String owner);

    // Archive view: active but archived.
    List<Reminder> findByOwnerAndIsActiveTrueAndArchivedTrueOrderByLastUpdatedAtDesc(String owner);

    // Trash view: soft-deleted.
    List<Reminder> findByOwnerAndIsActiveFalseOrderByLastUpdatedAtDesc(String owner);

    Optional<Reminder> findByIdAndOwnerAndIsActiveTrue(String id, String owner);

    // Load regardless of active/archived state — for restore / archive / purge.
    Optional<Reminder> findByIdAndOwner(String id, String owner);

    /**
     * Due, still-pending reminders that want an email and haven't been emailed
     * yet — the scheduler's work queue.
     */
    List<Reminder> findByIsActiveTrueAndEmailReminderTrueAndEmailSentAtIsNullAndStatusAndRemindAtLessThanEqual(
            ReminderStatus status, Long now);
}
