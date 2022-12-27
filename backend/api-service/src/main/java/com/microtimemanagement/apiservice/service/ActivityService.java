package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.model.Activity;

import java.util.Calendar;
import java.util.List;

public interface ActivityService {
    public List<Activity> makeFromRecordLogRequestDTO(ActivityRecordCreationRequestDTO activityRecordCreationRequestDTO);
    public Calendar buildCalendarInstanceFromDateAndTime(String recordDate, List<Integer> timeComponents);

    public List<Integer> generateTimeComponentsListFromHourMinuteString(String hourMinute);


}
