package com.microtimemanagement.apiservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordLogRequestDTO {

    private String recordDate;

    private String activityName;

    private String activityDescription;

    private String activityStartHourMinutes;

    private String activityEndHourMinutes;

    private Boolean isNextDaySpan = Boolean.FALSE;

}
