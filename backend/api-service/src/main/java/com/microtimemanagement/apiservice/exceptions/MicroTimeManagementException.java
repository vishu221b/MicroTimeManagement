package com.microtimemanagement.apiservice.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@Slf4j
@ToString
public class MicroTimeManagementException extends RuntimeException{

    private List<String> errors;

    public MicroTimeManagementException(String message){
        super(message);
    }
    public MicroTimeManagementException(String message, List<String> errors){
        super(message);
        setErrors(errors);
    }
    public MicroTimeManagementException(){
        super();
    }
}
