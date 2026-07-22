package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.TimeMeridian;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mtm_activity")
@EqualsAndHashCode(callSuper = true)
public class Activity extends BaseModel {

    // Application-assigned (see ActivityRecordServiceImpl#setUniqueIdForActivity).
    @Id
    private String id;

    private String activityName;

    @Column(length = 2000)
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

    @Enumerated(EnumType.STRING)
    private TimeMeridian startTimeMeridian;

    @Enumerated(EnumType.STRING)
    private TimeMeridian endTimeMeridian;

    private String activityDate;

    // Optional attached image, stored as a data-URL base64 string (<= 5 MB).
    @Column(columnDefinition = "text")
    private String imageBase64;

}
