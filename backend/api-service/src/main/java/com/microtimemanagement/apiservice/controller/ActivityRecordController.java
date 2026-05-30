package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.constants.PaginationConstants;
import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.request.ActivityUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityHistoryItemDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityNamesResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityStatsResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationRequestFieldsSanitizationResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationResultResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.service.ActivityRecordService;
import com.microtimemanagement.apiservice.utils.ApiUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiConstants.ActivityEndpoint.API_BASE)
@Tag(name = "Activity Record", description = "Activity Record Operations")
public class ActivityRecordController {

    /**
     * Get records by record date for user
     * create/update new record
     */


    private final ActivityRecordService activityRecordService;

    @RequestMapping(value = ApiConstants.EMPTY_BASE, method = RequestMethod.POST)
    @ResponseBody
    public ActivityRecordCreationdResponseDTO saveRecord(
            @Valid @RequestBody ActivityRecordCreationRequestDTO activityRecordCreationRequestDTO
    ) throws MicroTimeManagementBadRequestException, ParseException {
        return activityRecordService.processCreateUpdateRequest(activityRecordCreationRequestDTO);
    }

    @RequestMapping(value = "/getAllForDate", method = RequestMethod.GET)
    @ResponseBody
    public ActivityRecordResponseDTO getActivitiesForDate(
            @RequestParam String date
    ){
        return activityRecordService.getActivitiesForDate(date);
    }

    @RequestMapping(value = ApiConstants.EMPTY_BASE, method = RequestMethod.PUT)
    @ResponseBody
    public ActivityRecordResponseDTO updateActivity(
            @RequestParam String date,
            @Valid @RequestBody ActivityUpdateRequestDTO updateRequest,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return activityRecordService.updateActivity(date, updateRequest);
    }


    @RequestMapping(value = ApiConstants.EMPTY_BASE, method = RequestMethod.DELETE)
    @ResponseBody
    public ActivityRecordResponseDTO deleteActivityById(
            @RequestParam String date,
            @RequestParam String recordId
    ){
        return activityRecordService.deleteActivity(date, recordId);
    }

    /**
     * Aggregated activity totals for the current user over a date window.
     * Both bounds are optional — when omitted the service defaults to the
     * rolling last 7 days ending today. Powers the dashboard summary.
     */
    @RequestMapping(value = "/stats", method = RequestMethod.GET)
    @ResponseBody
    public ActivityStatsResponseDTO getStats(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ){
        return activityRecordService.getActivityStats(from, to);
    }

    /**
     * Paginated history of every day the current user has tracked, newest first.
     * Returns lightweight per-day summaries (count + total minutes), not the
     * full activity list — clients deep-link into /activity/getAllForDate.
     */
    @RequestMapping(value = "/history", method = RequestMethod.GET)
    @ResponseBody
    public PaginationResultResponseDTO<ActivityHistoryItemDTO> getHistory(
            @RequestParam(name = "page", required = false, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) Integer pageSize
    ){
        PaginationRequestFieldsSanitizationResponseDTO sanitized = ApiUtils
                .sanitizePaginationRequestFields(pageNumber, pageSize);
        return activityRecordService.getActivityHistory(
                PageRequest.of(sanitized.getPageNumber(), sanitized.getPageSize())
        );
    }

    /**
     * Distinct activity names the current user has used before, ordered by
     * most-recent use. Powers the create/edit form autocomplete so users don't
     * retype recurring names like "Standup" or "Email".
     */
    @RequestMapping(value = "/names", method = RequestMethod.GET)
    @ResponseBody
    public ActivityNamesResponseDTO getActivityNames(){
        return activityRecordService.getActivityNamesForCurrentUser();
    }

}
