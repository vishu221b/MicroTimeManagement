package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.TimeMeridian;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Activity {

    private String id;

    private String activityName;

    private String activityDescription;

    private Long startTimeEpoch;

    private Long endTimeEpoch;

    private Integer startHourValue;

    private Integer startMinutesValue;

    private Integer endHourValue;

    private Integer endMinutesValue;

    private Long totalDurationInEpoch;

    private Long totalDurationInMinutes;

    private String totalDurationInHours;

    private TimeMeridian startTimeMeridian;

    private TimeMeridian endTimeMeridian;

    private String activityDate;

}
