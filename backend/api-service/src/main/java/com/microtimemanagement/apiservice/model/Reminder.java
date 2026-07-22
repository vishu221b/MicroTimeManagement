package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.LinkableType;
import com.microtimemanagement.apiservice.enums.ReminderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * A future, time-bound reminder owned by (scoped to) a single user. Can
 * optionally reference another entity (a task/activity/project) it's about.
 * When {@code emailReminder} is set, a scheduled job emails the owner once
 * {@code remindAt} passes (see ReminderEmailScheduler + EmailSender).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_reminder")
@EqualsAndHashCode(callSuper = true)
public class Reminder extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    @Column(length = 2000)
    private String notes;

    /** When to remind, epoch milliseconds. */
    private Long remindAt;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

    /** Also email the owner when it fires. */
    private Boolean emailReminder;

    /** Set once the reminder email has been sent, so we never double-send. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailSentAt;

    // Optional entity this reminder is about.
    @Enumerated(EnumType.STRING)
    private LinkableType linkedType;

    private String linkedId;

    /** Owning user's uid. */
    private String owner;
}
