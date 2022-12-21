package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.model.TimeRecord;
import com.microtimemanagement.apiservice.dto.request.RecordLogRequestDTO;
import com.microtimemanagement.apiservice.dto.response.RecordLogResponseDTO;

import java.text.ParseException;

public interface TimeRecordService {

    public void saveRecord(TimeRecord timeRecord);

    RecordLogResponseDTO processCreateUpdateRequest(RecordLogRequestDTO recordRequestBody) throws ParseException;
}
