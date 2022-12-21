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

    private Long startTimeEpoch;

    private Long endTimeEpoch;

    private String activityStartHourMinutes;

    private String activityEndHourMinutes;

}
