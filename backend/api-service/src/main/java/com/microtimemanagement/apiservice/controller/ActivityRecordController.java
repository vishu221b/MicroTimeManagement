package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.dto.request.ActivityRecordCreationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordCreationdResponseDTO;
import com.microtimemanagement.apiservice.dto.response.ActivityRecordResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.service.ActivityRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/activities")
@SecurityRequirement(name = "MTM Auth")
@Tag(name = "Activity Record", description = "Activity Record Operations")
public class ActivityRecordController {

    /**
     * Get records by record date for user
     * create/update new record
     */


    private final ActivityRecordService activityRecordService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public ActivityRecordCreationdResponseDTO saveRecord(
            @RequestBody ActivityRecordCreationRequestDTO activityRecordCreationRequestDTO
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

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    @ResponseBody
    public ActivityRecordResponseDTO deleteActivityById(
            @RequestParam String date,
            @RequestParam String recordId
    ){
        return activityRecordService.deleteActivity(date, recordId);
    }

}
