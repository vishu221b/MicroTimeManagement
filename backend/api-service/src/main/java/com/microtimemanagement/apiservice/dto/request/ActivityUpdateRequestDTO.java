package com.microtimemanagement.apiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUpdateRequestDTO {

    @NotBlank(message = "Activity id cannot be empty.")
    private String recordId;

    private String activityName;

    private String activityDescription;

    private String activityStartHourMinutes;

    private String activityEndHourMinutes;

    private Boolean isNextDaySpan;

    @Size(max = 7_000_000, message = "Image must be at most 5 MB.")
    private String imageBase64;

}
