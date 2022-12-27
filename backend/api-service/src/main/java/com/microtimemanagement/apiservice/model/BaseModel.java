package com.microtimemanagement.apiservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseModel {

    Boolean isActive;

    Date createdAt;

    Date lastUpdatedAt;

}
