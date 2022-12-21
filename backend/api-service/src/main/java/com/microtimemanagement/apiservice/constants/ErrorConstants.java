package com.microtimemanagement.apiservice.constants;

public class ErrorConstants {

    public static final String INVALID_DATE_VALUE = "Invalid date value. Please supply correctly formatted date.";
    public static final String OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY = """
            Overlapping activity time.
            Cannot create record as a record already exists with time falling in between the provided time span.
            """;

    public static final String START_ACTIVITY_TIME_OVERLAP_ERROR = "New activity should end before the existing activity's finish time for same date.";

    public static final String END_ACTIVITY_TIME_OVERLAP_ERROR = "New activity should begin later than last activity's finish time for same date.";
    public static final String ACTIVITY_STARTING_MORE_THAN_ENDING_ERROR = "Starting time cannot be later than the ending time for same date.";

}
