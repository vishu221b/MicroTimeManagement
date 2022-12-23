package com.microtimemanagement.apiservice.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class MicroTimeManagementException extends RuntimeException{

    public MicroTimeManagementException(String message){
        super(message);
//        log.info("MicroTimeManagementException : {}", message);
    }
    public MicroTimeManagementException(){
        super();
    }
}
