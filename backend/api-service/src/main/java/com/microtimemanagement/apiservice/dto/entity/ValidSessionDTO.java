package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidSessionDTO {

    private Boolean isValidSession;

    private User principal;

    private String error;

}