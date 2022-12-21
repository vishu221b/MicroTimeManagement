package com.microtimemanagement.apiservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microtimemanagement.apiservice.enums.TimeMeridian;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"totalDurationEpoch"})
public class Activity {

    private String activityName;

    private String activityDescription;

    private Long startTimeEpoch;

    private Long endTimeEpoch;

    private int startHourValue;

    private int startMinutesValue;

    private int endHourValue;

    private int endMinutesValue;

    private Long totalDurationInEpoch;

    private Long totalDurationInMinutes;

    private String totalDurationInHours;

    private TimeMeridian startTimeMeridian;

    private TimeMeridian endTimeMeridian;

    private String activityDate;

}
