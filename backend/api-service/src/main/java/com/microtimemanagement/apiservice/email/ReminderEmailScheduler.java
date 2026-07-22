package com.microtimemanagement.apiservice.email;

import com.microtimemanagement.apiservice.enums.ReminderStatus;
import com.microtimemanagement.apiservice.model.Reminder;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.ReminderRepository;
import com.microtimemanagement.apiservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Polls for due, still-pending reminders that asked for an email and dispatches
 * them via {@link EmailSender} (a logging stub by default), marking each so it
 * is never emailed twice. Poll interval is {@code mtm.reminders.email-poll-ms}
 * (default 60s).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderEmailScheduler {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Scheduled(fixedDelayString = "${mtm.reminders.email-poll-ms:60000}")
    public void dispatchDueReminderEmails() {
        List<Reminder> due = reminderRepository
                .findByIsActiveTrueAndEmailReminderTrueAndEmailSentAtIsNullAndStatusAndRemindAtLessThanEqual(
                        ReminderStatus.PENDING, System.currentTimeMillis());
        if (due.isEmpty()) {
            return;
        }
        log.info("Dispatching {} due reminder email(s).", due.size());
        for (Reminder reminder : due) {
            User owner = userRepository.findByUidAndIsActiveTrue(reminder.getOwner());
            if (owner != null && StringUtils.isNotBlank(owner.getEmail())) {
                emailSender.send(
                        owner.getEmail(),
                        "Reminder: " + StringUtils.defaultString(reminder.getTitle()),
                        StringUtils.defaultString(reminder.getNotes()));
            }
            reminder.setEmailSentAt(new Date());
            reminderRepository.save(reminder);
        }
    }
}
