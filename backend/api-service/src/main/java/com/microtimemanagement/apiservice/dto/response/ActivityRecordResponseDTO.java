package com.microtimemanagement.apiservice.dto.response;

import com.microtimemanagement.apiservice.dto.ActivityDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRecordResponseDTO {

    private String recordDate;

    private List<ActivityDTO> activities;

    private Date createdAt;

    private Date lastUpdatedAt;

}
