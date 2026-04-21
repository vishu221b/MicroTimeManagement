package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.service.impl.ActivityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivityServiceImplTest {

    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        activityService = new ActivityServiceImpl();
    }

    @Test
    void generateTimeComponentsListFromHourMinuteString() {
        String recordDateAM1 = "12:12:0|0:12:0";
        String recordDateAM2 = "1:12:0|1:12:0";
        String recordDateAM3 = "11:12:0|11:12:0";
        String recordDatePM1 = "12:12:1|12:12:1";
        String recordDatePM2 = "20:12:1|20:12:1";
        String recordDatePM3 = "08:12:1|20:12:1";
        List.of(recordDatePM1, recordDateAM2, recordDateAM3, recordDateAM1, recordDatePM2, recordDatePM3)
                .forEach(time -> {
                    List<Integer> components = activityService.generateTimeComponentsListFromHourMinuteString(time.split("\\|")[0]);
                    assertEquals(Arrays.stream(time.split("\\|")[1].split(":")).map(Integer::parseInt).toList(), components);
                });
    }

    @Test
    void makeFromRecordLogRequestDTO() {
    }

    @Test
    void buildCalendarInstanceFromDateAndTime() {
    }

    @Test
    void calculateActivityDurationInEpoch() {
    }

    @Test
    void calculateActivityMinutesFromMilliseconds() {
    }

    @Test
    void calculateActivityHourDurationStringFromMilliseconds() {
    }

    @Test
    void compareAndFilterAndUpdateHourMinutesTimeComponentList() {
    }

    @Test
    void getComponentsListForDate() {
    }
}