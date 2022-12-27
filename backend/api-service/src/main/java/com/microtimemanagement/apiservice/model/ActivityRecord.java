package com.microtimemanagement.apiservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "micro_activity_record")
public class ActivityRecord extends BaseModel{

    @Id
    private String id;

    private String recordDate;

    private List<Activity> activities;

    private String createdBy;

}
