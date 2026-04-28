package com.microtimemanagement.apiservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GenericMessageResponseDTO<T> {

    private T payload;

    private String message;

}
