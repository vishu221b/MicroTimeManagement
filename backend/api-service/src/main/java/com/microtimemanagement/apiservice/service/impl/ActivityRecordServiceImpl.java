package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.ActivityDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityHistoryItemDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityNamesResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityStatsResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationResultResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
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
            CreatePipelineResult pipelineResult = buildRecordsForCreate(recordRequestBody);
            saveMultipleRecords(pipelineResult.recordsToSave());
            return ActivityRecordCreationdResponseDTO.builder()
                    .activities(pipelineResult.processedActivities().stream()
                            .map(activityConverter::toDTO)
                            .toList())
                    .build();
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

    /**
     * Normalises a yyyy-MM-dd input string by zero-padding any single-digit
     * month/day component, so lookups against the stored ISO-8601 record date
     * succeed regardless of how the caller spelled the value
     * (e.g. {@code 2026-5-3} becomes {@code 2026-05-03}).
     */
    private String formatDateToCorrectStringValue(String date) {
        String[] splitDate = date.split("-");
        return Arrays.stream(splitDate)
                .map(part -> (date.indexOf(part) != 0 && part.length() == 1) ? "0" + part : part)
                .reduce((current, next) -> current + "-" + next)
                .orElseThrow(() -> new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_DATE_VALUE));
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

    private void setUniqueIdForActivity(Activity activity){
        activity.setId(UUID.randomUUID().toString());
    }

    /**
     * Run the create pipeline for every {@link Activity} the request expands into
     * (one for a normal log, two when the activity crosses midnight). For each
     * one we either mutate the user's existing record for that date (inserting
     * the new activity at the correct chronological position, rejecting any
     * duplicate / overlap) or build a fresh record around it. The bundle returns
     * both the records to persist and the flat list of processed activities so
     * the caller can build the response without re-walking the records.
     */
    private CreatePipelineResult buildRecordsForCreate(ActivityRecordCreationRequestDTO request) {
        List<Activity> newActivities = activityService.makeFromRecordLogRequestDTO(request);
        log.info("Processing {} activity(ies) into records: {}", newActivities.size(), newActivities);

        List<ActivityRecord> recordsToSave = new ArrayList<>(newActivities.size());
        for (Activity newActivity : newActivities) {
            recordsToSave.add(prepareRecordFor(newActivity, request));
        }
        return new CreatePipelineResult(recordsToSave, newActivities);
    }

    /**
     * Return the {@link ActivityRecord} that should be persisted to land
     * {@code newActivity}. If the user already has a record for the activity's
     * date the existing record is mutated in place and returned; otherwise a
     * fresh record wrapping just this activity is built.
     */
    private ActivityRecord prepareRecordFor(Activity newActivity, ActivityRecordCreationRequestDTO request) {
        log.info("Processing new activity: {}", newActivity);
        ActivityRecord existingRecord = getByRecordDate(newActivity.getActivityDate());

        if (existingRecord == null) {
            return ActivityRecord.builder()
                    .recordDate(newActivity.getActivityDate())
                    .activities(new ArrayList<>(List.of(newActivity)))
                    .createdBy(request.getUser().getUid())
                    .build();
        }

        log.info("Found existing time record for date {}: {}", newActivity.getActivityDate(), existingRecord);
        if (!insertIntoExistingRecord(newActivity, existingRecord)) {
            throw new MicroTimeManagementBadRequestException(
                    ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY);
        }
        existingRecord.setLastUpdatedAt(new Date());
        return existingRecord;
    }

    /**
     * Walk {@code record}'s activities to validate and insert {@code newActivity}
     * at the correct chronological position. Returns {@code true} on a successful
     * insertion, {@code false} when no valid position exists (the caller turns
     * the false into a {@code OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY}
     * error).
     *
     * <p>Throws {@link MicroTimeManagementBadRequestException} when the new
     * activity exactly matches an existing entry's time range, or when it
     * overlaps any existing entry.
     */
    private boolean insertIntoExistingRecord(Activity newActivity, ActivityRecord record) {
        long newStart = newActivity.getStartTimeEpoch();
        long newEnd = newActivity.getEndTimeEpoch();
        List<Activity> activities = record.getActivities();

        // First pass: reject duplicates and any half-open-interval overlap.
        // [newStart, newEnd) overlaps [existingStart, existingEnd) iff
        // newStart < existingEnd && newEnd > existingStart.
        for (Activity existing : activities) {
            long existingStart = existing.getStartTimeEpoch();
            long existingEnd = existing.getEndTimeEpoch();

            if (existingStart == newStart && existingEnd == newEnd) {
                throw new MicroTimeManagementBadRequestException("Record already exists for the set time.");
            }
            if (newStart < existingEnd && newEnd > existingStart) {
                throw new MicroTimeManagementBadRequestException(
                        ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY);
            }
        }

        // Insertion position: index of the first existing activity that starts
        // at or after newEnd. Because the list is kept sorted by start time and
        // we already rejected overlaps, this lands the new activity in the
        // correct chronological slot.
        int insertAt = 0;
        while (insertAt < activities.size()
                && activities.get(insertAt).getStartTimeEpoch() < newEnd) {
            insertAt++;
        }

        // Refuse to insert before a midnight-anchored first activity: nothing on
        // the same day can come earlier than 00:00. Preserved from the original
        // pipeline so callers see the existing overlap error on this edge.
        if (insertAt == 0 && !activities.isEmpty()
                && activityStartsAtMidnight(activities.get(0))) {
            return false;
        }

        log.info("Inserting activity at position {} in record list", insertAt);
        setUniqueIdForActivity(newActivity);
        activities.add(insertAt, newActivity);
        return true;
    }

    private static boolean activityStartsAtMidnight(Activity activity) {
        return Integer.valueOf(0).equals(activity.getStartHourValue())
                && Integer.valueOf(0).equals(activity.getStartMinutesValue());
    }

    /**
     * Bundles what the create pipeline produces: the records to persist and the
     * flat list of activities the request expanded into. Replaces the previous
     * null-{@code recordDate} "summary" record that was tacked onto the return
     * list as an implicit side channel for response construction.
     */
    private record CreatePipelineResult(
            List<ActivityRecord> recordsToSave,
            List<Activity> processedActivities
    ) {}

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

    @Override
    public PaginationResultResponseDTO<ActivityHistoryItemDTO> getActivityHistory(PageRequest pageRequest) {
        // Always sort by recordDate DESC regardless of what the controller passed —
        // recordDate is ISO-8601 strings so lexical sort matches calendar order, and
        // "newest day first" is the only ordering this view ever wants.
        PageRequest sorted = PageRequest.of(
                pageRequest.getPageNumber(),
                pageRequest.getPageSize(),
                Sort.by(Sort.Direction.DESC, "recordDate")
        );

        String userUid = userService.loadUserDTOByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).getUid();

        Page<ActivityRecord> page = activityRecordRepository.findByCreatedBy(userUid, sorted);

        List<ActivityHistoryItemDTO> items = page.getContent().stream()
                .map(this::toHistoryItem)
                .toList();

        return PaginationResultResponseDTO.<ActivityHistoryItemDTO>builder()
                .payload(items)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .sortingDirection(Sort.Direction.DESC.name())
                .sortedByFields(List.of("recordDate"))
                .build();
    }

    @Override
    public ActivityNamesResponseDTO getActivityNamesForCurrentUser() {
        String userUid = userService.loadUserDTOByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).getUid();

        // Walk every record newest-first so the most-recent variant of a
        // case-insensitively duplicated name wins the dedup ("Coding" logged
        // today beats "coding" from last month).
        List<ActivityRecord> records = activityRecordRepository
                .findByCreatedBy(userUid, Sort.by(Sort.Direction.DESC, "recordDate"));

        LinkedHashMap<String, String> firstSeen = new LinkedHashMap<>();
        for (ActivityRecord record : records) {
            if (record.getActivities() == null) continue;
            for (Activity activity : record.getActivities()) {
                if (activity == null) continue;
                String name = StringUtils.trimToNull(activity.getActivityName());
                if (name == null) continue;
                firstSeen.putIfAbsent(name.toLowerCase(), name);
            }
        }
        return ActivityNamesResponseDTO.builder()
                .names(new ArrayList<>(firstSeen.values()))
                .build();
    }

    private ActivityHistoryItemDTO toHistoryItem(ActivityRecord record) {
        List<Activity> activities = record.getActivities() == null
                ? List.of() : record.getActivities();
        long totalMinutes = activities.stream()
                .filter(Objects::nonNull)
                .mapToLong(a -> a.getTotalDurationInMinutes() == null
                        ? 0L : a.getTotalDurationInMinutes())
                .sum();
        return ActivityHistoryItemDTO.builder()
                .recordDate(record.getRecordDate())
                .activityCount(activities.size())
                .totalMinutes(totalMinutes)
                .totalDurationHuman(humanizeMinutes(totalMinutes))
                .lastUpdatedAt(record.getLastUpdatedAt())
                .build();
    }

}
