package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiPathConstants;
import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.service.ActivityRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "MTM Auth")
@RequestMapping(ApiPathConstants.ACTIVITY_BASE_ENDPOINT)
@Tag(name = "Activity Record", description = "Activity Record Operations")
public class ActivityRecordController {

    /**
     * Get records by record date for user
     * create/update new record
     */


    private final ActivityRecordService activityRecordService;

    @RequestMapping(value = ApiPathConstants.EMPTY_BASE, method = RequestMethod.POST)
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

    @RequestMapping(value = ApiPathConstants.EMPTY_BASE, method = RequestMethod.PUT)
    @ResponseBody
    public ActivityRecordResponseDTO updateActivity(
            @RequestParam String date
    ){
        return activityRecordService.updateActivity(date);
    }


    @RequestMapping(value = ApiPathConstants.EMPTY_BASE, method = RequestMethod.DELETE)
    @ResponseBody
    public ActivityRecordResponseDTO deleteActivityById(
            @RequestParam String date,
            @RequestParam String recordId
    ){
        return activityRecordService.deleteActivity(date, recordId);
    }

}
