package com.microtimemanagement.apiservice.handler;

import com.microtimemanagement.apiservice.dto.ExceptionDTO;
import com.microtimemanagement.apiservice.dto.MicroTimeManagementExceptionDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Date;

@Slf4j
@ControllerAdvice
public class MicroTimeManagementResourceExceptionHandler {

    @ExceptionHandler(value = {MicroTimeManagementBadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private @ResponseBody ExceptionDTO<?> handleBadRequestException(RuntimeException ex, ServletWebRequest webRequest){
        System.out.println(webRequest);
        System.out.println(webRequest.getRequest().getServletPath());
        System.out.println(webRequest.getParameterMap());
        return ExceptionDTO.builder().error(MicroTimeManagementExceptionDTO.builder()
                .errorMessage(ex.getMessage())
                .timestamp(new Date().getTime())
                .requestPath(webRequest.getRequest().getServletPath())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build()).build();
    }

}
