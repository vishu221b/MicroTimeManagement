package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.ActivityDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.ActivityDTO;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityStatsResponseDTO;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


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

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LocalDate parseIsoDateOrThrow(String value, String fieldLabel) {
        try {
            return LocalDate.parse(value, ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new MicroTimeManagementBadRequestException(
                    String.format("Invalid %s. Expected yyyy-MM-dd, got: %s", fieldLabel, value));
        }
    }

    private static String humanizeMinutes(long minutes) {
        if (minutes <= 0) return "0m";
        long h = minutes / 60;
        long m = minutes % 60;
        if (h == 0) return m + "m";
        if (m == 0) return h + "h";
        return h + "h " + m + "m";
    }

    @Override
    public ActivityStatsResponseDTO getActivityStats(String fromDate, String toDate) {
        // Default window: rolling 7 days ending today, inclusive on both ends.
        // If a caller supplies only one bound we anchor the other to it so
        // single-day lookups still work (?from=2026-05-20 → that one day).
        LocalDate today = LocalDate.now();
        LocalDate to = StringUtils.isNotBlank(toDate)
                ? parseIsoDateOrThrow(toDate, "toDate") : today;
        LocalDate from = StringUtils.isNotBlank(fromDate)
                ? parseIsoDateOrThrow(fromDate, "fromDate") : to.minusDays(6);
        if (from.isAfter(to)) {
            throw new MicroTimeManagementBadRequestException("fromDate must be on or before toDate.");
        }

        String userUid = userService.loadUserDTOByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).getUid();

        List<ActivityRecord> records = activityRecordRepository
                .findByCreatedByAndRecordDateBetween(userUid, from.format(ISO_DATE), to.format(ISO_DATE));

        // Flatten activities; defend against null lists from older / partially-migrated records.
        List<Activity> activities = records.stream()
                .filter(Objects::nonNull)
                .flatMap(r -> r.getActivities() == null
                        ? java.util.stream.Stream.<Activity>empty()
                        : r.getActivities().stream())
                .filter(Objects::nonNull)
                .toList();

        long totalMinutes = activities.stream()
                .mapToLong(a -> a.getTotalDurationInMinutes() == null
                        ? 0L : a.getTotalDurationInMinutes())
                .sum();

        int daysWithActivity = (int) records.stream()
                .filter(r -> r.getActivities() != null && !r.getActivities().isEmpty())
                .map(ActivityRecord::getRecordDate)
                .distinct()
                .count();

        long avgPerActiveDay = daysWithActivity == 0
                ? 0L : Math.round((double) totalMinutes / (double) daysWithActivity);

        // Group by name (case-insensitive trim) for the "top activities" breakdown.
        Map<String, ActivityStatsResponseDTO.ActivityBreakdownDTO> byName = new LinkedHashMap<>();
        for (Activity a : activities) {
            String key = StringUtils.lowerCase(StringUtils.trimToEmpty(a.getActivityName()));
            if (key.isEmpty()) key = "(unnamed)";
            long mins = a.getTotalDurationInMinutes() == null ? 0L : a.getTotalDurationInMinutes();
            ActivityStatsResponseDTO.ActivityBreakdownDTO entry = byName.get(key);
            if (entry == null) {
                entry = ActivityStatsResponseDTO.ActivityBreakdownDTO.builder()
                        .activityName(StringUtils.defaultIfBlank(a.getActivityName(), "(unnamed)"))
                        .occurrenceCount(0)
                        .totalMinutes(0L)
                        .build();
                byName.put(key, entry);
            }
            entry.setOccurrenceCount(entry.getOccurrenceCount() + 1);
            entry.setTotalMinutes(entry.getTotalMinutes() + mins);
        }
        List<ActivityStatsResponseDTO.ActivityBreakdownDTO> topByDuration = byName.values().stream()
                .peek(b -> b.setTotalDurationHuman(humanizeMinutes(b.getTotalMinutes())))
                .sorted(Comparator.comparingLong(
                        ActivityStatsResponseDTO.ActivityBreakdownDTO::getTotalMinutes).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Recent activities: newest day first, and within a day reverse the
        // stored chronological order so the most recent shows up at the top.
        List<Activity> recent = records.stream()
                .filter(r -> r.getActivities() != null)
                .sorted(Comparator.comparing(ActivityRecord::getRecordDate).reversed())
                .flatMap(r -> {
                    List<Activity> list = new ArrayList<>(r.getActivities());
                    Collections.reverse(list);
                    return list.stream();
                })
                .limit(5)
                .toList();

        return ActivityStatsResponseDTO.builder()
                .fromDate(from.format(ISO_DATE))
                .toDate(to.format(ISO_DATE))
                .daysWithActivity(daysWithActivity)
                .totalActivities(activities.size())
                .totalMinutes(totalMinutes)
                .totalDurationHuman(humanizeMinutes(totalMinutes))
                .averageMinutesPerActiveDay(avgPerActiveDay)
                .topActivitiesByDuration(topByDuration)
                .recentActivities(recent.stream().map(activityConverter::toDTO).toList())
                .build();
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
