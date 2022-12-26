package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.TimeMeridian;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Activity extends BaseModel{

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

    private String uid;

}
