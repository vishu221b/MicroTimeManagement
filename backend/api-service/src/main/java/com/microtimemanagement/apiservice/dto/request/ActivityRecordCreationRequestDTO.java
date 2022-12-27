package com.microtimemanagement.apiservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microtimemanagement.apiservice.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRecordCreationRequestDTO {

    private String recordDate;

    private String activityName;

    private String activityDescription;

    private String activityStartHourMinutes;

    private String activityEndHourMinutes;

    @JsonIgnore
    private Boolean isNextDaySpan = Boolean.FALSE;

    @JsonIgnore
    private UserDTO user;

}
