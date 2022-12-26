package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.dto.request.RecordLogRequestDTO;
import com.microtimemanagement.apiservice.dto.response.RecordLogActivityListResponseDTO;
import com.microtimemanagement.apiservice.dto.response.RecordLogResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.service.TimeRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.ParseException;

@Slf4j
@RestController
@RequestMapping("api/v1/timeRecord")
@SecurityRequirement(name = "MTM Auth")
@EnableMethodSecurity(securedEnabled = true)
public class TimeRecordController {

    /**
     * Get records by record date for user
     * Get all records for user
     * create/update new record
     */

    @Autowired
    TimeRecordService timeRecordService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public RecordLogResponseDTO saveRecord(@RequestBody RecordLogRequestDTO recordLogRequestDTO) throws MicroTimeManagementBadRequestException, ParseException {
        return timeRecordService.processCreateUpdateRequest(recordLogRequestDTO);
    }

    @RequestMapping(value = "/getForDate", method = RequestMethod.GET)
    @ResponseBody
    public RecordLogActivityListResponseDTO getRecordsForDate(
            @RequestParam String date, Principal principal
    ){
        log.info("{}", principal.getName());
        log.info("{}", principal);
        return timeRecordService.getActivitiesForDate(date);
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    @ResponseBody
    public RecordLogActivityListResponseDTO deleteRecordById(
            @RequestParam String date, @RequestParam String recordId
    ){
        return RecordLogActivityListResponseDTO.builder().build();
    }

}
