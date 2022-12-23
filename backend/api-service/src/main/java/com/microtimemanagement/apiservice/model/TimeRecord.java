package com.microtimemanagement.apiservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document("micro_time_record")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRecord {

    @Id
    private String id;

    private String recordForDate;

    private List<Activity> activities;

    private Date createdAt;

    private Date lastUpdatedAt;

}
