package com.microtimemanagement.apiservice.exceptions;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
public class MicroTimeManagementBadRequestException extends MicroTimeManagementException{

    public MicroTimeManagementBadRequestException(String message){
        super(message);
        log.info("MicroTimeManagementBadRequestException: {}", message);
    }

    public MicroTimeManagementBadRequestException(String message, Class<?> className){
        super(message);
        log.info("MicroTimeManagementBadRequestException: {} at: {}", message, className);
    }

    public MicroTimeManagementBadRequestException(String customMessage, String originalMessage, Class<?> className){
        super(customMessage);
        log.info("MicroTimeManagementBadRequestException with, custom message: {}, original message {}, at: {}",
                customMessage,
                originalMessage,
                className
        );
    }

    public MicroTimeManagementBadRequestException(){
        super();
    }
}
