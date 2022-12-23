package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.dto.request.RecordLogRequestDTO;
import com.microtimemanagement.apiservice.dto.response.RecordLogActivityListResponseDTO;
import com.microtimemanagement.apiservice.dto.response.RecordLogResponseDTO;
import com.microtimemanagement.apiservice.service.TimeRecordService;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/timeRecord")
public class TimeRecordController {

    /**
     * Get records by record date for user
     * Get all records for user
     * create/update new record
     */

    @Autowired
    TimeRecordService timeRecordService;

    @RequestMapping(value = "/createNew", method = RequestMethod.POST)
    @ResponseBody
    public RecordLogResponseDTO saveRecord(@RequestBody RecordLogRequestDTO recordLogRequestDTO) throws MicroTimeManagementBadRequestException, ParseException {
        return timeRecordService.processCreateUpdateRequest(recordLogRequestDTO);
    }

    @RequestMapping(value = "/getForDate", method = RequestMethod.GET)
    @ResponseBody
    public RecordLogActivityListResponseDTO getRecordsForDate(
            @RequestParam String date
    ){
        return timeRecordService.getActivitiesForDate(date);
    }

}
