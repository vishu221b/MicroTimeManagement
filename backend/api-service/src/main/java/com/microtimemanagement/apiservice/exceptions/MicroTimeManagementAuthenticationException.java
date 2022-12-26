package com.microtimemanagement.apiservice.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
public class MicroTimeManagementAuthenticationException extends MicroTimeManagementException{

    public MicroTimeManagementAuthenticationException(String message){
        super(message);
        log.info("MicroTimeManagementBadRequestException: {}", message);
    }

    public MicroTimeManagementAuthenticationException(String message, Class<?> className){
        super(message);
        log.info("MicroTimeManagementBadRequestException: {} at: {}", message, className);
    }

    public MicroTimeManagementAuthenticationException(String customMessage, String originalMessage, Class<?> className){
        super(customMessage);
        log.info("MicroTimeManagementBadRequestException with, custom message: {}, original message {}, at: {}",
                customMessage,
                originalMessage,
                className
        );
    }

    public MicroTimeManagementAuthenticationException(){
        super();
    }
}
