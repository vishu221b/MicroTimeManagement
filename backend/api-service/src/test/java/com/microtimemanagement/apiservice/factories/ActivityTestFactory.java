package com.microtimemanagement.apiservice.factories;

import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityUpdateRequestDTO;
import com.microtimemanagement.apiservice.enums.TimeMeridian;
import com.microtimemanagement.apiservice.model.Activity;
import com.microtimemanagement.apiservice.model.ActivityRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActivityTestFactory {

    public static class Defaults {
        public static final String RECORD_DATE = "2026-05-26";
        public static final String CREATED_BY_UID = "test-user-uid";
        public static final String ACTIVITY_NAME = "Focus block";
        public static final String ACTIVITY_DESCRIPTION = "Deep work";
        public static final String START_HOUR_MINUTES = "09:00:0"; // 9:00 AM (Calendar.AM = 0)
        public static final String END_HOUR_MINUTES = "10:00:0";   // 10:00 AM
    }

    public static ActivityRecordCreationRequestDTO creationRequest() {
        return creationRequest(
                Defaults.RECORD_DATE,
                Defaults.ACTIVITY_NAME,
                Defaults.ACTIVITY_DESCRIPTION,
                Defaults.START_HOUR_MINUTES,
                Defaults.END_HOUR_MINUTES
        );
    }

    public static ActivityRecordCreationRequestDTO creationRequest(
            String recordDate,
            String name,
            String description,
            String startHourMinutes,
            String endHourMinutes
    ) {
        ActivityRecordCreationRequestDTO dto = new ActivityRecordCreationRequestDTO();
        dto.setRecordDate(recordDate);
        dto.setActivityName(name);
        dto.setActivityDescription(description);
        dto.setActivityStartHourMinutes(startHourMinutes);
        dto.setActivityEndHourMinutes(endHourMinutes);
        dto.setIsNextDaySpan(Boolean.FALSE);
        return dto;
    }

    public static ActivityUpdateRequestDTO updateMetadataRequest(String recordId, String name, String description) {
        return ActivityUpdateRequestDTO.builder()
                .recordId(recordId)
                .activityName(name)
                .activityDescription(description)
                .build();
    }

    public static ActivityUpdateRequestDTO updateRetimeRequest(
            String recordId,
            String name,
            String description,
            String startHourMinutes,
            String endHourMinutes
    ) {
        return ActivityUpdateRequestDTO.builder()
                .recordId(recordId)
                .activityName(name)
                .activityDescription(description)
                .activityStartHourMinutes(startHourMinutes)
                .activityEndHourMinutes(endHourMinutes)
                .isNextDaySpan(Boolean.FALSE)
                .build();
    }

    /**
     * Builds an Activity entity for tests. Times are expressed as 24-hour wall clock values
     * (startHour24, endHour24) so meridian + epoch derivations are kept in one place.
     */
    public static Activity activity(
            String recordDate,
            int startHour24,
            int startMinutes,
            int endHour24,
            int endMinutes
    ) {
        long startEpoch = wallClockEpochMillis(recordDate, startHour24, startMinutes);
        long endEpoch = wallClockEpochMillis(recordDate, endHour24, endMinutes);

        TimeMeridian startMeridian = startHour24 < 12 ? TimeMeridian.AM : TimeMeridian.PM;
        TimeMeridian endMeridian = endHour24 < 12 ? TimeMeridian.AM : TimeMeridian.PM;

        int startHourValue = startHour24 == 0 ? 12 : (startHour24 > 12 ? startHour24 - 12 : startHour24);
        int endHourValue = endHour24 == 0 ? 12 : (endHour24 > 12 ? endHour24 - 12 : endHour24);

        return Activity.builder()
                .id(UUID.randomUUID().toString())
                .activityName(Defaults.ACTIVITY_NAME)
                .activityDescription(Defaults.ACTIVITY_DESCRIPTION)
                .activityDate(recordDate)
                .startTimeEpoch(startEpoch)
                .endTimeEpoch(endEpoch)
                .startHourValue(startHourValue)
                .startMinutesValue(startMinutes)
                .endHourValue(endHourValue)
                .endMinutesValue(endMinutes)
                .startTimeMeridian(startMeridian)
                .endTimeMeridian(endMeridian)
                .totalDurationInEpoch(endEpoch - startEpoch)
                .totalDurationInMinutes((endEpoch - startEpoch) / 60000L)
                .totalDurationInHours("01Hr:00Min")
                .build();
    }

    public static ActivityRecord recordForUser(String userId, String date, Activity... activities) {
        List<Activity> list = new ArrayList<>(List.of(activities));
        return ActivityRecord.builder()
                .id(UUID.randomUUID().toString())
                .recordDate(date)
                .createdBy(userId)
                .activities(list)
                .isActive(Boolean.TRUE)
                .build();
    }

    private static long wallClockEpochMillis(String date, int hour24, int minutes) {
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);
        return new java.util.Calendar.Builder()
                .setDate(year, month, day)
                .setTimeOfDay(hour24, minutes, 0)
                .build()
                .getTimeInMillis();
    }
}
