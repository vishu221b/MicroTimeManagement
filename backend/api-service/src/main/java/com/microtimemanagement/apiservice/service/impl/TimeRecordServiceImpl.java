package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.ActivityConverter;
import com.microtimemanagement.apiservice.converter.TimeRecordConverter;
import com.microtimemanagement.apiservice.dto.ActivityDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import com.microtimemanagement.apiservice.model.Activity;
import com.microtimemanagement.apiservice.model.TimeRecord;
import com.microtimemanagement.apiservice.dto.request.RecordLogRequestDTO;
import com.microtimemanagement.apiservice.dto.response.RecordLogResponseDTO;
import com.microtimemanagement.apiservice.repository.TimeRecordRepository;
import com.microtimemanagement.apiservice.service.ActivityService;
import com.microtimemanagement.apiservice.service.TimeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@Service
public class TimeRecordServiceImpl implements TimeRecordService {

    @Autowired
    TimeRecordRepository timeRecordRepository;

    @Autowired
    TimeRecordConverter timeRecordConverter;

    @Autowired
    ActivityService activityService;

    @Autowired
    ActivityConverter activityConverter;

    @Override
    public void saveRecord(TimeRecord timeRecord) {
        TimeRecord dbTimeRecord = getByRecordDate(timeRecord.getRecordForDate());
        log.info("Record:{}", dbTimeRecord);
        if(null != dbTimeRecord){
            dbTimeRecord.getActivities().add(timeRecord.getActivities().get(0));
            timeRecord = dbTimeRecord;
        }
        timeRecordRepository.save(timeRecord);
    }

    public TimeRecord getByRecordDate(String recordDate){
        return timeRecordRepository.findByRecordForDate(recordDate).orElse(null);
    }
    private void saveMultipleRecords(List<TimeRecord> records){
//        records = records.stream().map(timeRecord -> {
//            TimeRecord dbRecord = getByRecordDate(timeRecord.getRecordForDate());
//            if(null!=dbRecord && dbRecord.getRecordForDate().equals(timeRecord.getRecordForDate())){
//
//                dbRecord.getActivities().add(timeRecord.getActivities().get(0));
//                return dbRecord;
//            }
//            return timeRecord;
//        }).toList();

        timeRecordRepository.saveAll(records);
    }

    @Override
    public RecordLogResponseDTO processCreateUpdateRequest(RecordLogRequestDTO recordRequestBody) throws MicroTimeManagementException, ParseException {
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsed = simpleDateFormat.parse(recordRequestBody.getRecordDate());
            log.info("Parsed Record Date: {}", parsed);
            log.info("Parsed Record Date: {}", parsed.getTime());
//            TimeRecord timeRecord = timeRecordRepository.findByRecordForDate(recordRequestBody.getRecordDate()).orElse(null);
//            if(null!=timeRecord){
//                Long lastActivityEpoch = timeRecord.getLastActivityEndTimeEpoch();
//                long startMillis = activityService.buildCalendarInstanceFromDateAndTime(
//                        recordRequestBody.getRecordDate(),
//                        activityService.generateTimeComponentsListFromHourMinuteString(
//                                recordRequestBody.getActivityStartHourMinutes()
//                        )
//                ).getTimeInMillis();
//                if(startMillis <= lastActivityEpoch){
//                    throw new MicroTimeManagementBadRequestException(
//                            ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY
//                    );
//                }
//            }
            List<TimeRecord> timeRecords = makeFromRecordLogRequestDTO(recordRequestBody);
            saveMultipleRecords(timeRecords);
            return convertToRecordLogResponseDTO(timeRecords);
        }
        catch (Exception ex){
            if(ex.getClass().equals(ParseException.class)){
                throw new MicroTimeManagementBadRequestException(
                        ErrorConstants.INVALID_DATE_VALUE,
                        ex.getLocalizedMessage(),
                        this.getClass()
                );
            }
            if(ex.getClass().equals(MicroTimeManagementBadRequestException.class)){
                throw new MicroTimeManagementBadRequestException(
                        ex.getMessage(),
                        ex.getLocalizedMessage(),
                        this.getClass()
                );
            }
            throw ex;
        }
    }

    private void insertActivityInTimeRecordAtLocation(Activity activity, TimeRecord timeRecord, int position){
        log.info("Adding activity to record list at position: {}", position);
        timeRecord.getActivities().add(position, activity);
    }
    public List<TimeRecord> makeFromRecordLogRequestDTO(RecordLogRequestDTO recordLogRequestDTO){
        List<Activity> activities = activityService.makeFromRecordLogRequestDTO(recordLogRequestDTO);
        log.info("Processing activities to Time Records....");
        log.info("{}", activities);
        log.info("{}", activities.size());
        return activities.stream().map(
                newActivity -> {
                    log.info("Processing: {}", newActivity);
                    TimeRecord timeRecord = getByRecordDate(newActivity.getActivityDate());
                    if (null!= timeRecord){
                        log.info("Processing existing time record: {}", timeRecord);

                        List<Activity> recordActivityList = timeRecord.getActivities();
                        Calendar dateOffset = activityService.buildCalendarInstanceFromDateAndTime(newActivity.getActivityDate(), Arrays.asList(0, 0,0));
                        int recordActivityListSize= recordActivityList.size();
                        boolean isError = Boolean.FALSE;
                        int counter= 0;

                        while(counter < recordActivityListSize &!isError){

                            Activity current = recordActivityList.get(counter);

                            // New activity after first activity
                            if(current.getStartTimeEpoch().equals(dateOffset.getTimeInMillis())
                                    && newActivity.getStartTimeEpoch() > current.getEndTimeEpoch()){
                                insertActivityInTimeRecordAtLocation(newActivity, timeRecord, counter+1);
                                return timeRecord;
                            }

                            // New Activity can be anywhere, First/Last/Between
                            if(current.getStartTimeEpoch() > dateOffset.getTimeInMillis()){
                                // Between
                                if ((counter + 1) < recordActivityListSize){
                                    Activity next = recordActivityList.get(counter+1);
                                    if (next.getStartTimeEpoch() > newActivity.getEndTimeEpoch()
                                            && current.getEndTimeEpoch() < newActivity.getStartTimeEpoch()){
                                        insertActivityInTimeRecordAtLocation(newActivity, timeRecord, counter);
                                        return timeRecord;
                                    }
                                } else if (
                                        current.getStartTimeEpoch() > newActivity.getEndTimeEpoch()
                                                && (counter == recordActivityListSize-1)
                                ) { // Last
                                    insertActivityInTimeRecordAtLocation(newActivity, timeRecord, counter);
                                    return timeRecord;
                                } else if (
                                        current.getEndTimeEpoch() < newActivity.getStartTimeEpoch()
                                                && counter == 0
                                ) { // First
                                    insertActivityInTimeRecordAtLocation(newActivity, timeRecord, counter+1);
                                    return timeRecord;
                                }
                            }
                            counter++;
                        }
                        throw new MicroTimeManagementBadRequestException(
                                ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY);
                    }
                    return TimeRecord.builder()
                            .recordForDate(newActivity.getActivityDate())
                            .lastActivityEndTimeEpoch(newActivity.getEndTimeEpoch())
                            .activities(List.of(newActivity)).build();
                }).toList();
    }
    public RecordLogResponseDTO convertToRecordLogResponseDTO(List<TimeRecord> timeRecords){
        List<ActivityDTO> activities = new ArrayList<>();
        timeRecords.forEach(
                timeRecord -> {
                    Activity activity = timeRecord.getActivities().get(timeRecord.getActivities().size() - 1);
                    activities.add(activityConverter.toDTO(activity));
                }
        );
        return RecordLogResponseDTO.builder().activities(activities).build();

    }

}
