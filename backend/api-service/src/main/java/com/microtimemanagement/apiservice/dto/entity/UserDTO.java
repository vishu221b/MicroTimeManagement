package com.microtimemanagement.apiservice.dto.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microtimemanagement.apiservice.model.ActivityRecord;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @NotEmpty(message = "Id cannot be empty.")
    private String id;

    private String firstName;

    private String lastName;

    private String username;

    @JsonIgnore
    private String password;

    @NotEmpty(message = "Uid cannot be empty.")
    private String uid;

    private String email;

    private Date createdAt;

    private Date lastUpdatedAt;

    private Set<String> roles;

    @JsonIgnore
    private List<ActivityRecord> activityRecords;

    private Boolean isActive;

    private String dateOfBirth;

}
