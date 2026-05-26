package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.ActivityDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.ActivityDTO;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Activity;
import com.microtimemanagement.apiservice.model.ActivityRecord;
import com.microtimemanagement.apiservice.repository.ActivityRecordRepository;
import com.microtimemanagement.apiservice.service.ActivityRecordService;
import com.microtimemanagement.apiservice.service.ActivityService;
import com.microtimemanagement.apiservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityRecordServiceImpl implements ActivityRecordService {

    private final ActivityRecordRepository activityRecordRepository;

    private final ActivityService activityService;

    private final ActivityDTOConverter activityConverter;

    private final UserService userService;

    @Override
    public void saveRecordWithFirstActivity(ActivityRecord activityRecord) {
        ActivityRecord dbActivityRecord = getByRecordDate(activityRecord.getRecordDate());
        log.info("Record:{}", dbActivityRecord);
        if(null != dbActivityRecord){
            dbActivityRecord.getActivities().add(activityRecord.getActivities().get(0));
            activityRecord = dbActivityRecord;
        }
        activityRecordRepository.save(activityRecord);
    }

    private ActivityRecord saveRecord(ActivityRecord record){
        return activityRecordRepository.save(record);
    }

    public ActivityRecord getByRecordDate(String recordDate){
        return activityRecordRepository.findByRecordDate(recordDate).orElse(null);
    }
    private void saveMultipleRecords(List<ActivityRecord> records){
        activityRecordRepository.saveAll(records);
    }

    @Override
    public ActivityRecordCreationdResponseDTO processCreateUpdateRequest(ActivityRecordCreationRequestDTO recordRequestBody) throws MicroTimeManagementException, ParseException {
        try{
            log.info("Security Context holder is: {}", SecurityContextHolder.getContext().getAuthentication().getName());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsed = simpleDateFormat.parse(recordRequestBody.getRecordDate());
            log.info("Parsed Record Date: {}", parsed);
            log.info("Parsed Record Date: {}", parsed.getTime());
            recordRequestBody.setUser(userService.loadUserDTOByUsername(
                    SecurityContextHolder.getContext().getAuthentication().getName()
            ));
            List<ActivityRecord> activityRecords = makeFromRecordLogRequestDTO(recordRequestBody);
            saveMultipleRecords(activityRecords.stream().filter(r -> null!=r.getRecordDate()).toList());
            return convertToRecordLogResponseDTO(activityRecords);
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

    private String formatDateToCorrectStringValue(String date){
        String[] splitDate = date.split("-");
        return Arrays.stream(splitDate).map(
                part -> {
                    if(date.indexOf(part) != 0 && part.length() == 1){
                        return "0" + part;
                    }
                    return part;
                }
        ).reduce((current, next) -> current+"-"+next).toString()
                .replaceAll("Optional\\[", "")
                .replaceAll("]", "");
    }
    @Override
    public ActivityRecordResponseDTO getActivitiesForDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try{
            dateFormat.parse(date);
            date = formatDateToCorrectStringValue(date);
        }catch (ParseException e){
            e.printStackTrace();
            throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_DATE_VALUE);
        }
        log.info("Processing activities get request for date: {}", date);
        UserDTO userDTO = userService.loadUserDTOByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        ActivityRecord activityRecord = activityRecordRepository.findByRecordDateAndCreatedBy(
                date, userDTO.getUid()
        ).orElse(null);
        if(null == activityRecord){
            throw new MicroTimeManagementBadRequestException(String.format("No record found for date %s", date));
        }
        return ActivityRecordResponseDTO.builder()
                .recordDate(activityRecord.getRecordDate())
                .createdAt(activityRecord.getCreatedAt())
                .lastUpdatedAt(activityRecord.getLastUpdatedAt())
                .activities(activityRecord.getActivities().stream()
                        .map(activityConverter::toDTO)
                        .toList()
                )
                .build();
    }

    @Override
    public ActivityRecordResponseDTO deleteActivity(String date, String recordId) {
        Optional<ActivityRecord> activityRecord = activityRecordRepository.findByRecordDateAndCreatedBy(
                date,
                userService.loadUserDTOByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName()
                ).getUid()
        );
        if(activityRecord.isEmpty()){
            throw new MicroTimeManagementNotFoundException(ErrorConstants.ACTIVITY_NOT_FOUND);
        }
        List<Activity> activities = activityRecord.get().getActivities();
        activities.remove(
                activities.stream()
                        .filter(activity -> activity.getId().equals(recordId))
                        .findFirst()
                        .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.ACTIVITY_NOT_FOUND))
        );
        activityRecord.get().setActivities(activities);
        saveRecord(activityRecord.get());
        return ActivityRecordResponseDTO.builder()
                .recordDate(activityRecord.get().getRecordDate())
                .activities(
                        activityRecord.get().getActivities().stream().map(activityConverter::toDTO).toList()
                )
                .createdAt(activityRecord.get().getCreatedAt())
                .lastUpdatedAt(activityRecord.get().getLastUpdatedAt())
                .build();
    }

    @Override
    public ActivityRecordResponseDTO updateActivity(String date, ActivityUpdateRequestDTO updateRequest) {
        UserDTO userDTO = userService.loadUserDTOByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        ActivityRecord activityRecord = activityRecordRepository
                .findByRecordDateAndCreatedBy(date, userDTO.getUid())
                .orElseThrow(() -> new MicroTimeManagementNotFoundException(
                        String.format("No record found for date %s", date)));

        Activity existing = activityRecord.getActivities().stream()
                .filter(a -> a.getId().equals(updateRequest.getRecordId()))
                .findFirst()
                .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.ACTIVITY_NOT_FOUND));

        boolean retime = StringUtils.isNotBlank(updateRequest.getActivityStartHourMinutes())
                && StringUtils.isNotBlank(updateRequest.getActivityEndHourMinutes());

        if (retime) {
            // Remove the existing entry, then re-insert via the create pipeline so overlap
            // validation and chronological ordering stay in one place. If the record becomes
            // empty, delete it outright — leaving an empty record around would make the
            // create pipeline trip its "existing record + nothing inserted" guard and falsely
            // report an overlap.
            activityRecord.getActivities().removeIf(a -> a.getId().equals(updateRequest.getRecordId()));
            if (activityRecord.getActivities().isEmpty()) {
                activityRecordRepository.delete(activityRecord);
            } else {
                saveRecord(activityRecord);
            }

            ActivityRecordCreationRequestDTO creationDTO = new ActivityRecordCreationRequestDTO();
            creationDTO.setRecordDate(date);
            creationDTO.setActivityName(StringUtils.defaultIfBlank(
                    updateRequest.getActivityName(), existing.getActivityName()));
            creationDTO.setActivityDescription(StringUtils.defaultIfBlank(
                    updateRequest.getActivityDescription(), existing.getActivityDescription()));
            creationDTO.setActivityStartHourMinutes(updateRequest.getActivityStartHourMinutes());
            creationDTO.setActivityEndHourMinutes(updateRequest.getActivityEndHourMinutes());
            creationDTO.setIsNextDaySpan(Boolean.TRUE.equals(updateRequest.getIsNextDaySpan()));
            try {
                processCreateUpdateRequest(creationDTO);
            } catch (MicroTimeManagementException e) {
                throw e;
            } catch (Exception e) {
                throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_DATE_VALUE);
            }
        } else {
            if (StringUtils.isNotBlank(updateRequest.getActivityName())) {
                existing.setActivityName(updateRequest.getActivityName());
            }
            if (null != updateRequest.getActivityDescription()) {
                existing.setActivityDescription(updateRequest.getActivityDescription());
            }
            saveRecord(activityRecord);
        }
        return getActivitiesForDate(date);
    }

    private ActivityRecord insertActivityInTimeRecordAtLocation(
            Activity activity, ActivityRecord activityRecord, int position, AtomicBoolean activityUpdateStatus
    ){
        log.info("Adding activity to record list at position: {}", position);
        setUniqueIdForActivity(activity);
        activityRecord.getActivities().add(position, activity);
        setActivityUpdatedToTrue(activityUpdateStatus);
        return activityRecord;
    }

    private void setUniqueIdForActivity(Activity activity){
        activity.setId(UUID.randomUUID().toString());
    }

    private void setActivityUpdatedToTrue(AtomicBoolean status){
        status.set(Boolean.TRUE);
    }
    public List<ActivityRecord> makeFromRecordLogRequestDTO(ActivityRecordCreationRequestDTO activityRecordCreationRequestDTO){
        List<Activity> activities = activityService.makeFromRecordLogRequestDTO(activityRecordCreationRequestDTO);
        log.info("Processing activities to Records....");
        log.info("{}", activities);
        log.info("{}", activities.size());
        AtomicBoolean activityUpdated = new AtomicBoolean(Boolean.FALSE);
        var ref = new Object() {
            ActivityRecord existingTimeRecord = null;
        };
        List<ActivityRecord> activityRecordList = new ArrayList<>(activities.stream().map(
                newActivity -> {
                    log.info("Processing new: {}", newActivity);

                    ActivityRecord activityRecord = getByRecordDate(newActivity.getActivityDate());

                    Long newActivityStartTime = newActivity.getStartTimeEpoch();
                    Long newActivityEndTime = newActivity.getEndTimeEpoch();

                    if (null!= activityRecord){
                        log.info("Processing existing time record: {}", activityRecord);

                        ref.existingTimeRecord = activityRecord;

                        List<Activity> recordActivityList = activityRecord.getActivities();

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
                                            newActivity, activityRecord, recordActivityList.indexOf(next), activityUpdated
                                    );
                                }
                                if(newActivityEndTime <= currentActivityStartTime && !(
                                        current.getStartHourValue().equals(0) && current.getStartMinutesValue().equals(0)
                                )){
                                    log.info("Inserting at first position w.r.t current activity...");
                                    return insertActivityInTimeRecordAtLocation(
                                            newActivity, activityRecord, recordActivityList.indexOf(current), activityUpdated);
                                }
                            }
                            if(!activityIterator.hasNext()){
                                log.info("Iterator does not have next...");
                                if(null!=next){
                                    if(newActivityStartTime >= nextActivityEndTime){
                                        log.info("Inserting at last position w.r.t next activity...");
                                        return insertActivityInTimeRecordAtLocation(
                                                newActivity, activityRecord, recordActivityList.indexOf(next)+1, activityUpdated);
                                    }
                                    if(newActivityEndTime <= currentActivityStartTime && !(
                                            current.getStartHourValue().equals(0) && current.getStartMinutesValue().equals(0)
                                            )){
                                        log.info("Inserting at first position w.r.t current activity...");
                                        return insertActivityInTimeRecordAtLocation(
                                                newActivity, activityRecord, recordActivityList.indexOf(current), activityUpdated);
                                    }
                                }
                                else if(newActivityStartTime >= currentActivityEndTime){
                                    log.info("Inserting at last position w.r.t current activity...");
                                    return insertActivityInTimeRecordAtLocation(
                                            newActivity, activityRecord, recordActivityList.indexOf(current)+1, activityUpdated);
                                }else if(
                                        newActivityEndTime <= currentActivityStartTime
                                                && !(current.getStartHourValue().equals(0) && current.getStartMinutesValue().equals(0))
                                ){
                                    log.info("Inserting at first position...");
                                    return insertActivityInTimeRecordAtLocation(
                                            newActivity, activityRecord, recordActivityList.indexOf(current), activityUpdated);
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
                        activityRecord.setLastUpdatedAt(new Date());
                    }
                    log.info("Ref: Status: {}, Record: {}", activityUpdated.get(), ref.existingTimeRecord);
                    if(null!=ref.existingTimeRecord && !activityUpdated.get()){
                        throw new MicroTimeManagementBadRequestException(
                                ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY);
                    }
                    return ActivityRecord.builder()
                            .recordDate(newActivity.getActivityDate())
                            .activities(List.of(newActivity))
                            .createdBy(activityRecordCreationRequestDTO.getUser().getUid()).build();
                }).toList());
        activityRecordList.add(ActivityRecord.builder().activities(activities).recordDate(null).build());
        return activityRecordList;
    }

    private String formatEpoch(Long epoch){
        return DateFormatUtils.format(epoch, "yyyy-MM-dd HH:mm:ss");
    }
    public ActivityRecordCreationdResponseDTO convertToRecordLogResponseDTO(List<ActivityRecord> activityRecords){
        List<ActivityDTO> activities = new ArrayList<>();
        activityRecords.stream()
                .filter(timeRecord -> null == timeRecord.getRecordDate())
                .findFirst()
                .ifPresent(
                        timeRecord -> timeRecord.getActivities()
                                .forEach(activity -> activities.add(activityConverter.toDTO(activity)))
                );
        return ActivityRecordCreationdResponseDTO.builder().activities(activities).build();

    }

}
