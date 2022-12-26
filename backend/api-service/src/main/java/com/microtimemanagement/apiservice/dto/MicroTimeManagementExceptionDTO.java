package com.microtimemanagement.apiservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicroTimeManagementExceptionDTO {

    private String message;

    private List<String> errors;

}
