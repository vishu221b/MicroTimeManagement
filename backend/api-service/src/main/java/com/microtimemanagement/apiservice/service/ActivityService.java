package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.request.RecordLogRequestDTO;
import com.microtimemanagement.apiservice.model.Activity;

import java.util.Calendar;
import java.util.List;

public interface ActivityService {
    public List<Activity> makeFromRecordLogRequestDTO(RecordLogRequestDTO recordLogRequestDTO);
    public Calendar buildCalendarInstanceFromDateAndTime(String recordDate, List<Integer> timeComponents);

    public List<Integer> generateTimeComponentsListFromHourMinuteString(String hourMinute);


}
