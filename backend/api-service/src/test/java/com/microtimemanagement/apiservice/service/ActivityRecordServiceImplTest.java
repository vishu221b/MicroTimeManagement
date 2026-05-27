package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.ActivityDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityStatsResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.factories.ActivityTestFactory;
import com.microtimemanagement.apiservice.factories.UserTestFactory;
import com.microtimemanagement.apiservice.model.Activity;
import com.microtimemanagement.apiservice.model.ActivityRecord;
import com.microtimemanagement.apiservice.repository.ActivityRecordRepository;
import com.microtimemanagement.apiservice.service.impl.ActivityRecordServiceImpl;
import com.microtimemanagement.apiservice.service.impl.ActivityServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("Activity Record Service Tests")
@ExtendWith(MockitoExtension.class)
public class ActivityRecordServiceImplTest {

    @Mock
    private ActivityRecordRepository activityRecordRepository;

    @Spy
    private ActivityServiceImpl activityService;

    @Spy
    private ActivityDTOConverter activityConverter;

    @Mock
    private UserService userService;

    @InjectMocks
    private ActivityRecordServiceImpl activityRecordService;

    private UserDTO authenticatedUser;

    @BeforeEach
    void setUpAuthenticatedPrincipal() {
        authenticatedUser = UserTestFactory.existingAppUserDTO().build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser.getUsername(), null, List.of()
                )
        );
        Mockito.lenient()
                .when(userService.loadUserDTOByUsername(authenticatedUser.getUsername()))
                .thenReturn(authenticatedUser);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // -- processCreateUpdateRequest ---------------------------------------

    @Test
    @DisplayName("Should create a brand new activity record when none exists for the date.")
    void shouldCreateNewActivityRecord() throws ParseException {
        ActivityRecordCreationRequestDTO request = ActivityTestFactory.creationRequest();
        Mockito.when(activityRecordRepository.findByRecordDate(request.getRecordDate()))
                .thenReturn(Optional.empty());

        ActivityRecordCreationdResponseDTO response = activityRecordService.processCreateUpdateRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getActivities()).hasSize(1);
        assertThat(response.getActivities().get(0).getActivityName())
                .isEqualTo(ActivityTestFactory.Defaults.ACTIVITY_NAME);

        ArgumentCaptor<List<ActivityRecord>> savedRecords =
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(activityRecordRepository).saveAll(savedRecords.capture());
        List<ActivityRecord> persisted = savedRecords.getValue();
        assertThat(persisted).hasSize(1);
        assertThat(persisted.get(0).getCreatedBy()).isEqualTo(authenticatedUser.getUid());
        assertThat(persisted.get(0).getRecordDate()).isEqualTo(request.getRecordDate());
    }

    @Test
    @DisplayName("Should append a non-overlapping activity to an existing record for the same date.")
    void shouldAppendNonOverlappingActivityToExistingRecord() throws ParseException {
        Activity earlierActivity = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 7, 0, 8, 0
        );
        ActivityRecord existingRecord = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                earlierActivity
        );
        Mockito.when(activityRecordRepository.findByRecordDate(ActivityTestFactory.Defaults.RECORD_DATE))
                .thenReturn(Optional.of(existingRecord));

        ActivityRecordCreationRequestDTO request = ActivityTestFactory.creationRequest();

        ActivityRecordCreationdResponseDTO response =
                activityRecordService.processCreateUpdateRequest(request);

        assertThat(response.getActivities()).hasSize(1);
        assertThat(existingRecord.getActivities()).hasSize(2);
        // Existing activity should stay first since it ends before the new one starts.
        assertThat(existingRecord.getActivities().get(0).getId()).isEqualTo(earlierActivity.getId());
    }

    @Test
    @DisplayName("Should reject an activity that exactly matches an existing time range.")
    void shouldRejectDuplicateTimeRangeActivity() {
        Activity existingActivity = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 0
        );
        ActivityRecord existingRecord = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                existingActivity
        );
        Mockito.when(activityRecordRepository.findByRecordDate(ActivityTestFactory.Defaults.RECORD_DATE))
                .thenReturn(Optional.of(existingRecord));

        ActivityRecordCreationRequestDTO request = ActivityTestFactory.creationRequest();

        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> activityRecordService.processCreateUpdateRequest(request))
                .withMessageContaining("Record already exists");
    }

    @Test
    @DisplayName("Should reject an activity whose time range overlaps with an existing activity.")
    void shouldRejectOverlappingActivity() {
        Activity existing = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 30
        );
        ActivityRecord existingRecord = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                existing
        );
        Mockito.when(activityRecordRepository.findByRecordDate(ActivityTestFactory.Defaults.RECORD_DATE))
                .thenReturn(Optional.of(existingRecord));

        // New range 10:00 - 11:00 overlaps existing 09:00-10:30 at 10:00..10:30.
        ActivityRecordCreationRequestDTO request = ActivityTestFactory.creationRequest(
                ActivityTestFactory.Defaults.RECORD_DATE,
                "Conflicting block",
                "Should fail",
                "10:00:0",
                "11:00:0"
        );

        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> activityRecordService.processCreateUpdateRequest(request))
                .withMessageContaining(ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY);
    }

    @Test
    @DisplayName("Should reject creation when the record date is not a valid yyyy-MM-dd value.")
    void shouldRejectInvalidDateString() {
        ActivityRecordCreationRequestDTO request = ActivityTestFactory.creationRequest(
                "not-a-date",
                ActivityTestFactory.Defaults.ACTIVITY_NAME,
                ActivityTestFactory.Defaults.ACTIVITY_DESCRIPTION,
                ActivityTestFactory.Defaults.START_HOUR_MINUTES,
                ActivityTestFactory.Defaults.END_HOUR_MINUTES
        );

        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> activityRecordService.processCreateUpdateRequest(request))
                .withMessageContaining(ErrorConstants.INVALID_DATE_VALUE);
    }

    // -- getActivitiesForDate ----------------------------------------------

    @Test
    @DisplayName("Should return the activity record for the given date scoped to the current user.")
    void shouldReturnActivitiesForDate() {
        Activity activity = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 0
        );
        ActivityRecord record = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                activity
        );
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.of(record));

        ActivityRecordResponseDTO response =
                activityRecordService.getActivitiesForDate(ActivityTestFactory.Defaults.RECORD_DATE);

        assertThat(response.getRecordDate()).isEqualTo(ActivityTestFactory.Defaults.RECORD_DATE);
        assertThat(response.getActivities()).hasSize(1);
        assertThat(response.getActivities().get(0).getId()).isEqualTo(activity.getId());
    }

    @Test
    @DisplayName("Should throw bad request when no activity record exists for the date.")
    void shouldThrowBadRequestWhenNoRecordForDate() {
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> activityRecordService.getActivitiesForDate(ActivityTestFactory.Defaults.RECORD_DATE))
                .withMessageContaining("No record found for date");
    }

    // -- deleteActivity ----------------------------------------------------

    @Test
    @DisplayName("Should delete a single activity from the record and persist the update.")
    void shouldDeleteActivityFromRecord() {
        Activity activity = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 0
        );
        ActivityRecord record = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                activity
        );
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.of(record));
        Mockito.when(activityRecordRepository.save(Mockito.any(ActivityRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActivityRecordResponseDTO response = activityRecordService.deleteActivity(
                ActivityTestFactory.Defaults.RECORD_DATE, activity.getId()
        );

        assertThat(response.getActivities()).isEmpty();
        assertThat(record.getActivities()).isEmpty();
        Mockito.verify(activityRecordRepository).save(record);
    }

    @Test
    @DisplayName("Should throw not found when deleting from a date that has no record.")
    void shouldThrowNotFoundWhenDeletingFromMissingRecord() {
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> activityRecordService.deleteActivity(
                        ActivityTestFactory.Defaults.RECORD_DATE, "missing-id"))
                .withMessage(ErrorConstants.ACTIVITY_NOT_FOUND);
    }

    @Test
    @DisplayName("Should throw not found when the activity id is not present in the day's record.")
    void shouldThrowNotFoundWhenDeletingUnknownActivityId() {
        Activity activity = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 0
        );
        ActivityRecord record = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                activity
        );
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.of(record));

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> activityRecordService.deleteActivity(
                        ActivityTestFactory.Defaults.RECORD_DATE, "no-such-activity"))
                .withMessage(ErrorConstants.ACTIVITY_NOT_FOUND);
    }

    // -- updateActivity ----------------------------------------------------

    @Test
    @DisplayName("Should update activity name and description in place when no new times are provided.")
    void shouldUpdateActivityMetadataInPlace() {
        Activity activity = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 0
        );
        ActivityRecord record = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                activity
        );
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.of(record));
        Mockito.when(activityRecordRepository.save(Mockito.any(ActivityRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActivityUpdateRequestDTO request = ActivityTestFactory.updateMetadataRequest(
                activity.getId(), "Renamed", "Updated description"
        );

        ActivityRecordResponseDTO response = activityRecordService.updateActivity(
                ActivityTestFactory.Defaults.RECORD_DATE, request
        );

        assertThat(response.getActivities()).hasSize(1);
        assertThat(activity.getActivityName()).isEqualTo("Renamed");
        assertThat(activity.getActivityDescription()).isEqualTo("Updated description");
        // Times unchanged
        assertThat(activity.getStartHourValue()).isEqualTo(9);
        assertThat(activity.getEndHourValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should re-time the sole activity in a record by deleting the empty record and re-creating via the pipeline.")
    void shouldRetimeActivityViaCreationPipeline() {
        Activity activity = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 0
        );
        ActivityRecord record = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                activity
        );
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.of(record));
        // Before delete: lookup returns the (now empty) record; after delete: empty so the
        // creation pipeline takes the "new record" path.
        java.util.concurrent.atomic.AtomicBoolean recordDeleted = new java.util.concurrent.atomic.AtomicBoolean(false);
        Mockito.when(activityRecordRepository.findByRecordDate(ActivityTestFactory.Defaults.RECORD_DATE))
                .thenAnswer(invocation -> recordDeleted.get() ? Optional.empty() : Optional.of(record));
        Mockito.doAnswer(invocation -> {
            recordDeleted.set(true);
            return null;
        }).when(activityRecordRepository).delete(Mockito.any(ActivityRecord.class));
        // Save may not be invoked on the empty-record path; keep it lenient so the test
        // doesn't fail purely on unused-stub strictness.
        Mockito.lenient()
                .when(activityRecordRepository.save(Mockito.any(ActivityRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActivityUpdateRequestDTO request = ActivityTestFactory.updateRetimeRequest(
                activity.getId(),
                "Renamed",
                "Updated description",
                "11:00:0",  // 11:00 AM
                "11:30:0"   // 11:30 AM (stay in AM so we don't trigger meridian wrap)
        );

        assertThatNoException().isThrownBy(() -> activityRecordService.updateActivity(
                ActivityTestFactory.Defaults.RECORD_DATE, request
        ));

        // Old empty record was deleted; the create pipeline persisted a fresh record.
        Mockito.verify(activityRecordRepository).delete(record);
        ArgumentCaptor<List<ActivityRecord>> savedRecords = ArgumentCaptor.forClass(List.class);
        Mockito.verify(activityRecordRepository).saveAll(savedRecords.capture());
        List<ActivityRecord> persisted = savedRecords.getValue();
        assertThat(persisted).hasSize(1);
        Activity replacement = persisted.get(0).getActivities().get(0);
        assertThat(replacement.getId()).isNotEqualTo(activity.getId());
        assertThat(replacement.getStartHourValue()).isEqualTo(11);
        assertThat(replacement.getEndHourValue()).isEqualTo(11);
        assertThat(replacement.getEndMinutesValue()).isEqualTo(30);
        assertThat(replacement.getActivityName()).isEqualTo("Renamed");
    }

    @Test
    @DisplayName("Should throw not found when updating an activity id that does not exist in the record.")
    void shouldThrowNotFoundWhenUpdatingUnknownActivityId() {
        Activity activity = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 0
        );
        ActivityRecord record = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                activity
        );
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.of(record));

        ActivityUpdateRequestDTO request = ActivityTestFactory.updateMetadataRequest(
                "no-such-activity", "Renamed", "Updated description"
        );

        assertThatExceptionOfType(MicroTimeManagementNotFoundException.class)
                .isThrownBy(() -> activityRecordService.updateActivity(
                        ActivityTestFactory.Defaults.RECORD_DATE, request))
                .withMessage(ErrorConstants.ACTIVITY_NOT_FOUND);
    }

    @Test
    @DisplayName("Should throw bad request when a re-time would overlap a different activity in the record.")
    void shouldRejectRetimeWhenItOverlapsOtherActivity() {
        // Blocker occupies 11:00 AM - 11:45 AM
        Activity blocker = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 11, 0, 11, 45
        );
        // Target occupies 9:00 AM - 10:00 AM
        Activity target = ActivityTestFactory.activity(
                ActivityTestFactory.Defaults.RECORD_DATE, 9, 0, 10, 0
        );
        ActivityRecord record = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(),
                ActivityTestFactory.Defaults.RECORD_DATE,
                target, blocker
        );
        Mockito.when(activityRecordRepository.findByRecordDateAndCreatedBy(
                        ActivityTestFactory.Defaults.RECORD_DATE, authenticatedUser.getUid()))
                .thenReturn(Optional.of(record));
        Mockito.when(activityRecordRepository.findByRecordDate(ActivityTestFactory.Defaults.RECORD_DATE))
                .thenAnswer(invocation -> Optional.of(record));
        Mockito.when(activityRecordRepository.save(Mockito.any(ActivityRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Try to move target into 11:15 AM - 11:30 AM, which overlaps the blocker (11:00 - 11:45 AM).
        ActivityUpdateRequestDTO request = ActivityTestFactory.updateRetimeRequest(
                target.getId(),
                "Renamed",
                "Updated description",
                "11:15:0",
                "11:30:0"
        );

        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> activityRecordService.updateActivity(
                        ActivityTestFactory.Defaults.RECORD_DATE, request))
                .withMessageContaining(ErrorConstants.OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY);
    }

    // -- getActivityStats --------------------------------------------------

    @Test
    @DisplayName("Should aggregate totals, daysWithActivity, top-by-duration, and recent activities for a window.")
    void shouldAggregateActivityStatsAcrossWindow() {
        String dayOne = "2026-05-20";
        String dayTwo = "2026-05-22";

        // dayOne: a 60-minute "Focus block" and a 30-minute "Meetings".
        Activity focusOne = ActivityTestFactory.activity(dayOne, 9, 0, 10, 0);
        focusOne.setActivityName("Focus block");
        Activity meetings = ActivityTestFactory.activity(dayOne, 10, 30, 11, 0);
        meetings.setActivityName("Meetings");
        meetings.setTotalDurationInMinutes(30L);
        ActivityRecord dayOneRecord = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(), dayOne, focusOne, meetings);

        // dayTwo: a 90-minute "Focus block" — should bubble to top of the breakdown.
        Activity focusTwo = ActivityTestFactory.activity(dayTwo, 9, 0, 10, 30);
        focusTwo.setActivityName("Focus block");
        focusTwo.setTotalDurationInMinutes(90L);
        ActivityRecord dayTwoRecord = ActivityTestFactory.recordForUser(
                authenticatedUser.getUid(), dayTwo, focusTwo);

        Mockito.when(activityRecordRepository.findByCreatedByAndRecordDateBetween(
                        authenticatedUser.getUid(), dayOne, dayTwo))
                .thenReturn(List.of(dayOneRecord, dayTwoRecord));

        ActivityStatsResponseDTO stats =
                activityRecordService.getActivityStats(dayOne, dayTwo);

        assertThat(stats.getFromDate()).isEqualTo(dayOne);
        assertThat(stats.getToDate()).isEqualTo(dayTwo);
        assertThat(stats.getDaysWithActivity()).isEqualTo(2);
        assertThat(stats.getTotalActivities()).isEqualTo(3);
        assertThat(stats.getTotalMinutes()).isEqualTo(180L);
        assertThat(stats.getAverageMinutesPerActiveDay()).isEqualTo(90L);
        assertThat(stats.getTopActivitiesByDuration()).hasSize(2);
        assertThat(stats.getTopActivitiesByDuration().get(0).getActivityName())
                .isEqualTo("Focus block");
        assertThat(stats.getTopActivitiesByDuration().get(0).getTotalMinutes()).isEqualTo(150L);
        assertThat(stats.getTopActivitiesByDuration().get(1).getActivityName())
                .isEqualTo("Meetings");
        // Newest first; dayTwo's activity should lead the recent list.
        assertThat(stats.getRecentActivities()).isNotEmpty();
    }

    @Test
    @DisplayName("Should reject an unparseable from/to date.")
    void shouldRejectInvalidStatsDate() {
        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> activityRecordService.getActivityStats("not-a-date", "2026-05-26"))
                .withMessageContaining("Invalid fromDate");
    }

    @Test
    @DisplayName("Should reject a window where fromDate is after toDate.")
    void shouldRejectInvertedWindow() {
        assertThatExceptionOfType(MicroTimeManagementBadRequestException.class)
                .isThrownBy(() -> activityRecordService.getActivityStats("2026-05-26", "2026-05-20"))
                .withMessageContaining("fromDate must be on or before toDate");
    }

    @Test
    @DisplayName("Should return zero-valued stats when no records exist in the window.")
    void shouldReturnEmptyStatsWhenNoRecords() {
        Mockito.when(activityRecordRepository.findByCreatedByAndRecordDateBetween(
                        Mockito.eq(authenticatedUser.getUid()), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(List.of());

        ActivityStatsResponseDTO stats =
                activityRecordService.getActivityStats("2026-05-20", "2026-05-26");

        assertThat(stats.getDaysWithActivity()).isZero();
        assertThat(stats.getTotalActivities()).isZero();
        assertThat(stats.getTotalMinutes()).isZero();
        assertThat(stats.getAverageMinutesPerActiveDay()).isZero();
        assertThat(stats.getTopActivitiesByDuration()).isEmpty();
        assertThat(stats.getRecentActivities()).isEmpty();
        assertThat(stats.getTotalDurationHuman()).isEqualTo("0m");
    }
}
