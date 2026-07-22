package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.enums.LinkableType;
import com.microtimemanagement.apiservice.enums.ReminderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderDTO {

    private String id;
    private String title;
    private String notes;
    private Long remindAt;
    private ReminderStatus status;
    private Boolean emailReminder;
    private LinkableType linkedType;
    private String linkedId;
    private Date createdAt;
    private Date lastUpdatedAt;
    private Boolean isActive;
}
