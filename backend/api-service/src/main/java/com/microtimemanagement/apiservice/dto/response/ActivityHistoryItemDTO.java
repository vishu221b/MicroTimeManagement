package com.microtimemanagement.apiservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * One-row summary of a user's activity for a single day. Powers the paginated
 * history view — the goal is "did I track time on this day, how much, how many
 * entries" without shipping the full activity list. Click-through on the
 * frontend then loads the full record via /activity/getAllForDate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityHistoryItemDTO {

    private String recordDate;

    private Integer activityCount;

    private Long totalMinutes;

    private String totalDurationHuman;

    private Date lastUpdatedAt;
}
