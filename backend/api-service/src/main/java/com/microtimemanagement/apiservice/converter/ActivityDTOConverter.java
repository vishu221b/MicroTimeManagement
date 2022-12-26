package com.microtimemanagement.apiservice.converter;

import com.microtimemanagement.apiservice.dto.ActivityDTO;
import com.microtimemanagement.apiservice.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ActivityDTOConverter implements BaseDTOConverter<Activity, ActivityDTO> {

    @Override
    public ActivityDTO toDTO(Activity activity){
        log.info("Converting activity to DTO: {}", activity);
        return ActivityDTO.builder()
                .id(activity.getId())
                .activityName(activity.getActivityName())
                .activityDescription(activity.getActivityDescription())
                .activityStartTime(
                        processActivityHourMinuteValueToString(activity.getStartHourValue()
                        ) +":"+processActivityHourMinuteValueToString(
                                activity.getStartMinutesValue()
                        ) +":"+activity.getStartTimeMeridian()
                )
                .activityEndTime(
                        processActivityHourMinuteValueToString(activity.getEndHourValue()
                        ) +":"+processActivityHourMinuteValueToString(
                                activity.getEndMinutesValue()
                        ) +":"+activity.getEndTimeMeridian()
                )
                .activityTotalDuration(activity.getTotalDurationInHours())
                .activityDate(activity.getActivityDate())
                .build();
    }
    private String processActivityHourMinuteValueToString(int hourMinuteValue){
        String value = Integer.toString(hourMinuteValue);
        return value.length() == 1 ? "0"+value : value;
    }

    @Override
    public Activity fromDTO(ActivityDTO baseDTO) {
        return Activity.builder()
                .activityName(baseDTO.getActivityName())
                .activityDescription(baseDTO.getActivityDescription())
                .build();
    }

}
