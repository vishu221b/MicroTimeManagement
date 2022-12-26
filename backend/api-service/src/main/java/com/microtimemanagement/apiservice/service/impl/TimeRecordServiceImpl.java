package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.ActivityDTOConverter;
import com.microtimemanagement.apiservice.converter.TimeRecordConverter;
import com.microtimemanagement.apiservice.dto.ActivityDTO;
import com.microtimemanagement.apiservice.dto.response.RecordLogActivityListResponseDTO;
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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


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
    ActivityDTOConverter activityConverter;

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
        timeRecordRepository.saveAll(records);
    }

    @Override
    public RecordLogResponseDTO processCreateUpdateRequest(RecordLogRequestDTO recordRequestBody) throws MicroTimeManagementException, ParseException {
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsed = simpleDateFormat.parse(recordRequestBody.getRecordDate());
            log.info("Parsed Record Date: {}", parsed);
            log.info("Parsed Record Date: {}", parsed.getTime());
            List<TimeRecord> timeRecords = makeFromRecordLogRequestDTO(recordRequestBody);
            saveMultipleRecords(timeRecords.stream().filter(r -> null!=r.getRecordForDate()).toList());
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

    @Override
    public RecordLogActivityListResponseDTO getActivitiesForDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try{
            dateFormat.parse(date);
        }catch (ParseException e){
            e.printStackTrace();
            throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_DATE_VALUE);
        }
        TimeRecord timeRecord = timeRecordRepository.findByRecordForDate(date).orElse(null);
        if(null == timeRecord){
            throw new MicroTimeManagementBadRequestException(String.format("No record found for date %s", date));
        }
        return RecordLogActivityListResponseDTO.builder()
                .recordDate(timeRecord.getRecordForDate())
                .createdAt(timeRecord.getCreatedAt())
                .lastUpdatedAt(timeRecord.getLastUpdatedAt())
                .activities(timeRecord.getActivities().stream().map(activity -> activityConverter.toDTO(activity)).toList())
                .build();
    }

    private TimeRecord insertActivityInTimeRecordAtLocation(
            Activity activity, TimeRecord timeRecord, int position, AtomicBoolean activityUpdateStatus
    ){
        log.info("Adding activity to record list at position: {}", position);
        setUniqueIdForActivity(activity);
        timeRecord.getActivities().add(position, activity);
        setActivityUpdatedToTrue(activityUpdateStatus);
        return timeRecord;
    }

    private void setUniqueIdForActivity(Activity activity){
        activity.setId(UUID.randomUUID().toString());
    }

    private void setActivityUpdatedToTrue(AtomicBoolean status){
        status.set(Boolean.TRUE);
    }
    public List<TimeRecord> makeFromRecordLogRequestDTO(RecordLogRequestDTO recordLogRequestDTO){
        List<Activity> activities = activityService.makeFromRecordLogRequestDTO(recordLogRequestDTO);
        log.info("Processing activities to Time Records....");
        log.info("{}", activities);
        log.info("{}", activities.size());
        AtomicBoolean activityUpdated = new AtomicBoolean(Boolean.FALSE);
        var ref = new Object() {
            TimeRecord existingTimeRecord = null;
        };
        List<TimeRecord> timeRecordList = new ArrayList<>(activities.stream().map(
                newActivity -> {
                    log.info("Processing new: {}", newActivity);

                    TimeRecord timeRecord = getByRecordDate(newActivity.getActivityDate());

                    Long newActivityStartTime = newActivity.getStartTimeEpoch();
                    Long newActivityEndTime = newActivity.getEndTimeEpoch();

                    if (null!= timeRecord){
                        log.info("Processing existing time record: {}", timeRecord);

                        ref.existingTimeRecord = timeRecord;

                        List<Activity> recordActivityList = timeRecord.getActivities();

                        Iterator<Activity> activityIterator = recordActivityList.iterator();

                        Activity current = null, next = null;
                        while(activityIterator.hasNext()){
                            current = null != current ? current : activityIterator.next();
                            log.info("Current activity: {}", current);

                            Long currentActivityStartTime = current.getStartTimeEpoch();
                            Long currentActivityEndTime = current.getEndTimeEpoch();

                            if(currentActivityStartTime.equals(newActivityStartTime)
                                    && currentActivityEndTime.equals(newActivityEndTime)){
                                throw new MicroTimeManagementBadRequestException("Record already exists for the set time.");
                            }

                            if((newActivityStartTime >= currentActivityStartTime && newActivityStartTime < currentActivityEndTime)
                                    || (newActivityEndTime > currentActivityStartTime && newActivityEndTime <= currentActivityEndTime)
                            ){
                                throw new MicroTimeManagementBadRequestException(ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY);
                            }

                            Long nextActivityStartTime = 0L;
                            Long nextActivityEndTime = 0L;

                            if(activityIterator.hasNext()){
                                log.info("Handling the in-between case for the list.");
                                next = activityIterator.next();

                                nextActivityStartTime = next.getStartTimeEpoch();
                                nextActivityEndTime = next.getEndTimeEpoch();

                                log.info("Next activity: {}", next);

                                if(newActivityEndTime <= nextActivityStartTime
                                        && newActivityStartTime >= currentActivityEndTime){
                                    log.info("If the current element is first " +
                                            "element and new activity time range lies within this and next activity.");
                                    return insertActivityInTimeRecordAtLocation(
                                            newActivity, timeRecord, recordActivityList.indexOf(next), activityUpdated
                                    );
                                }
                                if(newActivityEndTime <= currentActivityStartTime && !(
                                        current.getStartHourValue().equals(0) && current.getStartMinutesValue().equals(0)
                                )){
                                    log.info("Inserting at first position w.r.t current activity...");
                                    return insertActivityInTimeRecordAtLocation(
                                            newActivity, timeRecord, recordActivityList.indexOf(current), activityUpdated);
                                }
                            }
                            if(!activityIterator.hasNext()){
                                log.info("Iterator does not have next...");
                                if(null!=next){
                                    if(newActivityStartTime >= nextActivityEndTime){
                                        log.info("Inserting at last position w.r.t next activity...");
                                        return insertActivityInTimeRecordAtLocation(
                                                newActivity, timeRecord, recordActivityList.indexOf(next)+1, activityUpdated);
                                    }
                                    if(newActivityEndTime <= currentActivityStartTime && !(
                                            current.getStartHourValue().equals(0) && current.getStartMinutesValue().equals(0)
                                            )){
                                        log.info("Inserting at first position w.r.t current activity...");
                                        return insertActivityInTimeRecordAtLocation(
                                                newActivity, timeRecord, recordActivityList.indexOf(current), activityUpdated);
                                    }
                                }
                                else if(newActivityStartTime >= currentActivityEndTime){
                                    log.info("Inserting at last position w.r.t current activity...");
                                    return insertActivityInTimeRecordAtLocation(
                                            newActivity, timeRecord, recordActivityList.indexOf(current)+1, activityUpdated);
                                }else if(
                                        newActivityEndTime <= currentActivityStartTime
                                                && !(current.getStartHourValue().equals(0) && current.getStartMinutesValue().equals(0))
                                ){
                                    log.info("Inserting at first position...");
                                    return insertActivityInTimeRecordAtLocation(
                                            newActivity, timeRecord, recordActivityList.indexOf(current), activityUpdated);
                                }

                            }
                            if(null!=next){
                                log.info("Current -> Start: {},End: {}",
                                        formatEpoch(currentActivityStartTime), formatEpoch(currentActivityEndTime));
                                log.info("New -> Start: {}, End: {}",
                                        formatEpoch(newActivityStartTime), formatEpoch(newActivityEndTime));
                                log.info("Next -> Start: {}, End: {}",
                                        formatEpoch(nextActivityStartTime), formatEpoch(nextActivityEndTime));
                                log.info("#################################");
                                log.info("| Assigning 'Next' to 'Current' |");
                                log.info("#################################");
                                current = next;
                                next = null;
                            }
                        }
                        timeRecord.setLastUpdatedAt(new Date());
                    }
                    log.info("Ref: Status: {}, Record: {}", activityUpdated.get(), ref.existingTimeRecord);
                    if(null!=ref.existingTimeRecord && !activityUpdated.get()){
                        throw new MicroTimeManagementBadRequestException(
                                ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY);
                    }
                    return TimeRecord.builder()
                            .recordForDate(newActivity.getActivityDate())
                            .activities(List.of(newActivity)).build();
                }).toList());
        timeRecordList.add(TimeRecord.builder().activities(activities).recordForDate(null).build());
        return timeRecordList;
    }

    private String formatEpoch(Long epoch){
        return DateFormatUtils.format(epoch, "yyyy-MM-dd HH:mm:ss");
    }
    public RecordLogResponseDTO convertToRecordLogResponseDTO(List<TimeRecord> timeRecords){
        List<ActivityDTO> activities = new ArrayList<>();
        timeRecords.stream()
                .filter(timeRecord -> null == timeRecord.getRecordForDate())
                .findFirst()
                .ifPresent(
                        timeRecord -> timeRecord.getActivities()
                                .forEach(activity -> activities.add(activityConverter.toDTO(activity)))
                );
        return RecordLogResponseDTO.builder().activities(activities).build();

    }

}
