package com.microtimemanagement.apiservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRecordCreationRequestDTO {

    private String recordDate;

    private String activityName;

    private String activityDescription;

    private String activityStartHourMinutes;

    private String activityEndHourMinutes;

    // Optional attached image as a data-URL base64 string. Cap ~7M chars so the
    // decoded payload stays under ~5 MB (base64 is ~4/3 the byte size).
    @Size(max = 7_000_000, message = "Image must be at most 5 MB.")
    private String imageBase64;

    @JsonIgnore
    private Boolean isNextDaySpan = Boolean.FALSE;

    @JsonIgnore
    private UserDTO user;

}
