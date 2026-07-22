package com.microtimemanagement.apiservice.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {

    private String id;

    private String activityName;

    private String activityDescription;

    private String activityDate;

    private String activityStartTime;

    private String activityEndTime;

    private String activityTotalDuration;

    private String imageBase64;

}
