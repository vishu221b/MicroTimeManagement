package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityStatsResponseDTO;
import com.microtimemanagement.apiservice.model.ActivityRecord;

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
}
