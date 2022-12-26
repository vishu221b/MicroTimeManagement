package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.dto.request.RecordLogRequestDTO;
import com.microtimemanagement.apiservice.enums.NextDayOffsetAction;
import com.microtimemanagement.apiservice.enums.TimeMeridian;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.model.Activity;
import com.microtimemanagement.apiservice.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    public List<Integer> generateTimeComponentsListFromHourMinuteString(String hourMinute){
        List<Integer> timeComponent = new java.util.ArrayList<>(Arrays.stream(StringUtils.split(
                hourMinute, ":"
        )).map(Integer::parseInt).toList());
        filterMeridianForTimeComponentList(timeComponent);
        updateHoursInTimeListForMeridianIfApplicable(timeComponent);
        return timeComponent;
    }

    @Override
    public List<Activity> makeFromRecordLogRequestDTO(RecordLogRequestDTO recordLogRequestDTO) {

        List<Integer> startHourMinuteList = generateTimeComponentsListFromHourMinuteString(
                recordLogRequestDTO.getActivityStartHourMinutes()
        );

        List<Integer> endHourMinuteList = generateTimeComponentsListFromHourMinuteString(
                recordLogRequestDTO.getActivityEndHourMinutes()
        );

        log.info("Time components list - Start: {} | End: {}", startHourMinuteList, endHourMinuteList);

        String recordDate = recordLogRequestDTO.getRecordDate();

        Calendar startDayCalendar = buildCalendarInstanceFromDateAndTime(recordDate, startHourMinuteList);

        Calendar endDayCalendar = buildCalendarInstanceFromDateAndTime(recordDate, endHourMinuteList);

        long difference = endDayCalendar.getTimeInMillis() - startDayCalendar.getTimeInMillis();

        log.info("Got time difference: {}", difference);
        log.info("Calendar Date Start: {}", startDayCalendar.getTime());
        log.info("Calendar Date End: {}", endDayCalendar.getTime());

        boolean isNextDay = isNextDayDateUseRequired(startHourMinuteList, endHourMinuteList) || recordLogRequestDTO.getIsNextDaySpan();

        if(difference < 0 && !isNextDay){
            throw new MicroTimeManagementBadRequestException("Activity cannot start after it's end time. Invalid time selected.");
        }

        if(difference > 12*60*60*1000){
            throw new MicroTimeManagementBadRequestException("An activity should be a maximum of 12 hours.");
        }

        if(difference==0){
            throw new MicroTimeManagementBadRequestException("Invalid time selected.");
        }

        if(isNextDay){
            // Offset is used to divide activities according to respective dates
            log.info("Using next day date::setting offset value");

            Long nextDayOffsetMillis = getNextDayCalendarInstanceForDateString(recordDate)
                    .getTimeInMillis();

            endDayCalendar.setTimeInMillis(endDayCalendar.getTimeInMillis() + DateUtils.MILLIS_PER_DAY);

            log.info("Formatted end date to: {}", endDayCalendar.getTime());
            log.info("Formatted end date to: {}", endDayCalendar.getTimeInMillis());

            String formattedEndDate = DateFormatUtils.format(endDayCalendar.getTimeInMillis(),"yyyy-MM-dd");

            Long activityFirstDayDurationMillis = calculateActivityDurationEpochWithOffsetUsingAction(
                    startDayCalendar.getTimeInMillis(), nextDayOffsetMillis, NextDayOffsetAction.SUBTRACT_FROM
            );

            Activity activityDayOne = Activity.builder()
                    .activityName(recordLogRequestDTO.getActivityName())
                    .activityDescription(recordLogRequestDTO.getActivityDescription())
                    .startTimeEpoch(startDayCalendar.getTimeInMillis())
                    .endTimeEpoch(nextDayOffsetMillis)
                    .startHourValue(startHourMinuteList.get(0))
                    .endHourValue(0)
                    .startMinutesValue(startHourMinuteList.get(1))
                    .endMinutesValue(0)
                    .startTimeMeridian(TimeMeridian.valueOf(startHourMinuteList.get(2)))
                    .endTimeMeridian(TimeMeridian.AM)
                    .totalDurationInEpoch(activityFirstDayDurationMillis)
                    .totalDurationInMinutes(
                            calculateActivityMinutesFromMilliseconds(activityFirstDayDurationMillis)
                    )
                    .totalDurationInHours(
                            calculateActivityHourDurationStringFromMilliseconds(activityFirstDayDurationMillis)
                    )
                    .activityDate(recordDate)
                    .uid(UUID.randomUUID().toString())
                    .build();

            Long activitySecondDayDurationMillis = calculateActivityDurationEpochWithOffsetUsingAction(
                    endDayCalendar.getTimeInMillis(), nextDayOffsetMillis, NextDayOffsetAction.SUBTRACT
            );

            Activity activityDayTwo = Activity.builder()
                    .activityName(recordLogRequestDTO.getActivityName())
                    .activityDescription(recordLogRequestDTO.getActivityDescription())
                    .startTimeEpoch(nextDayOffsetMillis)
                    .endTimeEpoch(endDayCalendar.getTimeInMillis())
                    .startHourValue(0)
                    .endHourValue(endHourMinuteList.get(0))
                    .startMinutesValue(0)
                    .endMinutesValue(endHourMinuteList.get(1))
                    .startTimeMeridian(TimeMeridian.AM)
                    .endTimeMeridian(TimeMeridian.valueOf(endHourMinuteList.get(2)))
                    .totalDurationInEpoch(activitySecondDayDurationMillis)
                    .totalDurationInMinutes(
                            calculateActivityMinutesFromMilliseconds(activitySecondDayDurationMillis)
                    )
                    .totalDurationInHours(
                            calculateActivityHourDurationStringFromMilliseconds(activitySecondDayDurationMillis)
                    )
                    .activityDate(formattedEndDate)
                    .uid(UUID.randomUUID().toString())
                    .build();

            return List.of(activityDayOne, activityDayTwo);
        }

        long activityDurationInEpoch = calculateActivityDurationInEpoch(
                        startHourMinuteList, endHourMinuteList
                );

        log.info("Split -> Start: {} | End: {}", startHourMinuteList, endHourMinuteList);

        return List.of(Activity.builder()
                .activityName(recordLogRequestDTO.getActivityName())
                .activityDescription(recordLogRequestDTO.getActivityDescription())
                .startTimeEpoch(
                        buildCalendarInstanceFromDateAndTime(recordDate, startHourMinuteList).getTimeInMillis()
                )
                .endTimeEpoch(
                        buildCalendarInstanceFromDateAndTime(recordDate, endHourMinuteList).getTimeInMillis()
                )
                .startHourValue(startHourMinuteList.get(0))
                .endHourValue(endHourMinuteList.get(0))
                .startMinutesValue(startHourMinuteList.get(1))
                .endMinutesValue(endHourMinuteList.get(1))
                .startTimeMeridian(TimeMeridian.valueOf(startHourMinuteList.get(2)))
                .endTimeMeridian(TimeMeridian.valueOf(endHourMinuteList.get(2)))
                .totalDurationInEpoch(activityDurationInEpoch)
                .totalDurationInMinutes(
                        calculateActivityMinutesFromMilliseconds(activityDurationInEpoch)
                )
                .totalDurationInHours(
                        calculateActivityHourDurationStringFromMilliseconds(activityDurationInEpoch)
                )
                .activityDate(recordDate)
                .uid(UUID.randomUUID().toString())
                .build());

    }

    public Calendar buildCalendarInstanceFromDateAndTime(String recordDate, List<Integer> timeComponents){
        List<Integer> date = getComponentsListForDate(recordDate);
        Calendar instance = new Calendar.Builder().setDate(
                date.get(0),date.get(1)-1, date.get(2)
        ).setTimeOfDay(timeComponents.get(0), timeComponents.get(1), 0).build();
        log.info("Built: ( {} ) from {} and {}", instance.getTime(), recordDate, timeComponents);
        return instance;
    }
    private Long calculateEpochFromHourMinuteInTimeComponentList(List<Integer> timeComponentList){
        return buildCalendarInstanceFromTimeComponents(
                timeComponentList
        ).getTimeInMillis();
    }


    private Long calculateActivityDurationEpochWithOffsetUsingAction(
            Long timeComponentListToMillis,
            Long offsetMillis,
            NextDayOffsetAction action
    ){
        if(action.equals(NextDayOffsetAction.SUBTRACT_FROM)){
            return offsetMillis - timeComponentListToMillis;
        }
        if(action.equals(NextDayOffsetAction.SUBTRACT)){
            return timeComponentListToMillis - offsetMillis;
        }
        return 0L;
    }

    public Long calculateActivityDurationInEpoch(
            List<Integer> startHourMinuteList, List<Integer> endHourMinuteList
    ){
        Long startTimeMillis = calculateEpochFromHourMinuteInTimeComponentList(startHourMinuteList);

        Long endTimeMillis = calculateEpochFromHourMinuteInTimeComponentList(endHourMinuteList);

        return endTimeMillis - startTimeMillis;
    }
    public Long calculateActivityMinutesFromMilliseconds(Long milliseconds){
        return TimeUnit.MILLISECONDS.toMinutes(milliseconds);
    }

    public String calculateActivityHourDurationStringFromMilliseconds(Long milliseconds){
        long activityDurationInMinutes = calculateActivityMinutesFromMilliseconds(milliseconds);

        long actualDurationInMinutes = activityDurationInMinutes < 60 ? activityDurationInMinutes : activityDurationInMinutes%60;

        String hours = String.valueOf(activityDurationInMinutes/60);

        return activityDurationInMinutes > 60
                ?  (hours.length() < 2 ? "0"+hours : hours)
                +"Hr:"+ actualDurationInMinutes+"Min"
                : "0Hr:"+activityDurationInMinutes+"Min";
    }
    private void updateHoursInTimeListForMeridianIfApplicable(List<Integer> timeComponentsList){
        if(timeComponentsList.get(2).equals(Calendar.AM) && timeComponentsList.get(0) == 12 ){
            timeComponentsList.set(0, 0);
        }
        if(timeComponentsList.get(2).equals(Calendar.PM) && timeComponentsList.get(0) < 12){
            Integer startHour = timeComponentsList.get(0);
            int newStartHour = startHour + 12;
            if (newStartHour > 23){
                newStartHour-=1;
            }
            timeComponentsList.set(0, newStartHour);
        }
    }

    private Calendar getNextDayCalendarInstanceForDateString(String date){
        log.info("Current date string: {}", date);
        Calendar calendarDate = buildCalendarInstanceForDateString(date);
        log.info("Calendar date current: {}", calendarDate.getTime());
        calendarDate.setTimeInMillis(calendarDate.getTimeInMillis() + DateUtils.MILLIS_PER_DAY);
        log.info("Added one to calendar date current: {}", calendarDate.getTime());
        return calendarDate;
    }

    private boolean isNextDayDateUseRequired(List<Integer> startHourMinuteList, List<Integer> endHourMinuteList){
        return (endHourMinuteList.get(2).equals(Calendar.AM) && startHourMinuteList.get(2).equals(Calendar.PM));
    }

    private Calendar buildCalendarInstanceFromTimeComponents(List<Integer> timeComponents){
        return new Calendar.Builder()
                .setTimeOfDay(
                        timeComponents.get(0),timeComponents.get(1), 0
                )
                .set(Calendar.AM_PM, timeComponents.get(2))
                .build();
    }
    public List<Integer> getComponentsListForDate(String date){
        return Arrays.stream(date.split("-")).map(Integer::parseInt).toList();
    }
    private Calendar buildCalendarInstanceForDateString(String date){
        List<Integer> dateList = getComponentsListForDate(date);
        log.info("Building Calendar instance for dateComponent: {}", dateList);
        return new Calendar.Builder()
                .setDate(dateList.get(0), dateList.get(1)-1, dateList.get(2)).build();
    }

    /**
     * @param timeComponentsList : List of Integers containing Hours, Minutes and Meridian from { Calendar.AM, Calendar.PM }
     * This method validates the values according to the meridian and sets the correct
     * hour value according to the 24-hour format
     */
    private void filterMeridianForTimeComponentList(List<Integer> timeComponentsList){
        if(timeComponentsList.get(2).equals(Calendar.AM) && timeComponentsList.get(0)>12){
            throw new MicroTimeManagementBadRequestException(
                    "Invalid date time received. Hour value cannot be greater than 12 for AM."
            );
        }
    }

}
