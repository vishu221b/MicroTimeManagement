package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityHistoryItemDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityNamesResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityStatsResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationResultResponseDTO;
import com.microtimemanagement.apiservice.model.ActivityRecord;
import org.springframework.data.domain.PageRequest;

import java.text.ParseException;

public interface ActivityRecordService {

    public void saveRecordWithFirstActivity(ActivityRecord activityRecord);

    ActivityRecordCreationdResponseDTO processCreateUpdateRequest(ActivityRecordCreationRequestDTO recordRequestBody) throws ParseException;

    ActivityRecordResponseDTO getActivitiesForDate(String date);

    ActivityRecordResponseDTO deleteActivity(String date, String recordId);

    ActivityRecordResponseDTO updateActivity(String date, ActivityUpdateRequestDTO updateRequest);

    /**
     * Aggregate the current user's activity records over an inclusive
     * [fromDate, toDate] window. Either bound may be null — the impl picks a
     * sensible default window (rolling 7 days) when both are null.
     */
    ActivityStatsResponseDTO getActivityStats(String fromDate, String toDate);

    /**
     * Paginated history of the current user's tracked days, newest day first.
     * Each item is a per-day roll-up (count + total minutes) rather than the
     * full activity list — the frontend deep-links into /activity for detail.
     */
    PaginationResultResponseDTO<ActivityHistoryItemDTO> getActivityHistory(PageRequest pageRequest);

    /**
     * Distinct activity names the current user has previously logged, ordered
     * by most-recent use first. Powers the create/edit form's autocomplete.
     * Names are deduped case-insensitively; the most-recent variant wins.
     */
    ActivityNamesResponseDTO getActivityNamesForCurrentUser();
}
