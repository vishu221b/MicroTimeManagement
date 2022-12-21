package com.microtimemanagement.apiservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicroTimeManagementExceptionDTO {

    private String errorMessage;

    private Long timestamp;

    private String requestPath;

    private int statusCode;

}
