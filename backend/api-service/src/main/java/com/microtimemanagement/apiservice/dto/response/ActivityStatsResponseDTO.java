package com.microtimemanagement.apiservice.dto.response;

import com.microtimemanagement.apiservice.dto.entity.ActivityDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated activity totals for a user over a date window. Used by the
 * dashboard summary widgets and any other consumer that wants a single roll-up
 * instead of paging through individual records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStatsResponseDTO {

    /** Inclusive start of the window, as yyyy-MM-dd. */
    private String fromDate;

    /** Inclusive end of the window, as yyyy-MM-dd. */
    private String toDate;

    /** Number of distinct dates in the window that have at least one activity. */
    private Integer daysWithActivity;

    /** Sum of activity counts across every record in the window. */
    private Integer totalActivities;

    /** Sum of every activity's duration, in minutes. */
    private Long totalMinutes;

    /** Pretty-printed totalMinutes, e.g. "7h 32m" — convenience for the UI. */
    private String totalDurationHuman;

    /** Average minutes per day-with-activity (zero if no activity in window). */
    private Long averageMinutesPerActiveDay;

    /** Activities ranked by total time spent, descending, capped client-side. */
    private List<ActivityBreakdownDTO> topActivitiesByDuration;

    /** Most recent activities across the window, newest first. */
    private List<ActivityDTO> recentActivities;

    /** Per-day totals for every date in the window (zeros included), oldest first. */
    private List<DailyStatDTO> dailyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStatDTO {
        /** The day, as yyyy-MM-dd. */
        private String date;
        /** Total minutes logged that day. */
        private Long totalMinutes;
        /** Number of activities logged that day. */
        private Integer activityCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityBreakdownDTO {
        /** Activity name (used as the grouping key). */
        private String activityName;
        /** Number of activity entries that share this name in the window. */
        private Integer occurrenceCount;
        /** Total minutes spent on this name across the window. */
        private Long totalMinutes;
        /** Pretty-printed totalMinutes for the UI. */
        private String totalDurationHuman;
    }
}
