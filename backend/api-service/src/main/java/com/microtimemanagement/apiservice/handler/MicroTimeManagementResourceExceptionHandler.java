package com.microtimemanagement.apiservice.handler;

import com.microtimemanagement.apiservice.dto.ExceptionDTO;
import com.microtimemanagement.apiservice.dto.MicroTimeManagementExceptionDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementAuthenticationException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Hidden
@ControllerAdvice
public class MicroTimeManagementResourceExceptionHandler {

    @ExceptionHandler(value = {MicroTimeManagementBadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private @ResponseBody ExceptionDTO<?> handleBadRequestException(MicroTimeManagementBadRequestException ex, ServletWebRequest webRequest){
        System.out.println(webRequest.getRequest().getRequestURI());
        System.out.println(webRequest.getUserPrincipal());
        System.out.println(webRequest.getRequest().getServletPath());
        log.error("{}", getBuilderForStackTrace(ex.getStackTrace()));
        return buildExceptionDTO(ex, webRequest, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(value = {MicroTimeManagementAuthenticationException.class})
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    private @ResponseBody ExceptionDTO<?> handleAuthException(MicroTimeManagementAuthenticationException ex, ServletWebRequest webRequest){
//        System.out.println(webRequest.getRequest().getRequestURI());
//        System.out.println(webRequest.getUserPrincipal());
//        System.out.println(webRequest.getRequest().getServletPath());
//        log.error("{}", getBuilderForStackTrace(ex.getStackTrace()));
//        return buildExceptionDTO(ex, webRequest, HttpStatus.NOT_FOUND);
//    }
    @ExceptionHandler(value = {MicroTimeManagementNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private @ResponseBody ExceptionDTO<?> handleNotFoundException(MicroTimeManagementNotFoundException ex, ServletWebRequest webRequest){
        System.out.println(webRequest.getRequest().getRequestURI());
        System.out.println(webRequest.getUserPrincipal());
        System.out.println(webRequest.getRequest().getServletPath());
        log.error("{}", getBuilderForStackTrace(ex.getStackTrace()));
        return buildExceptionDTO(ex, webRequest, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {MicroTimeManagementException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private @ResponseBody ExceptionDTO<?> handleMTMInternalServerException(MicroTimeManagementException ex, ServletWebRequest webRequest){
        log.error("{}", getBuilderForStackTrace(ex.getStackTrace()));
        return buildExceptionDTO(ex, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private @ResponseBody ExceptionDTO<?> handleInternalServerException(RuntimeException ex, ServletWebRequest webRequest){
        log.error("{}", getBuilderForStackTrace(ex.getStackTrace()));
        return buildExceptionDTO(ex, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    private @ResponseBody ExceptionDTO<?> handleAnyException(Exception ex, ServletWebRequest request){
        log.error("{}", getBuilderForStackTrace(ex.getStackTrace()));
        return buildExceptionDTO(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private StringBuilder getBuilderForStackTrace(StackTraceElement[] ex){
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(ex).forEach(s ->  stringBuilder.append(s).append("\n"));
        return stringBuilder;
    }

    private ExceptionDTO<?> buildExceptionDTO(
            Exception ex,
            ServletWebRequest request,
            HttpStatus status){
        if(ex instanceof MicroTimeManagementException exc){
            return ExceptionDTO.builder()
                    .error(
                            MicroTimeManagementExceptionDTO.builder()
                                    .message(ex.getMessage() + " Please contact dev.")
                                    .errors(
                                            null!= exc.getErrors() && exc.getErrors().size() > 0
                                            ? exc.getErrors() : Collections.emptyList())
                                    .build()
                    )
                    .code(new Date().getTime())
                    .path(request.getRequest().getServletPath())
                    .statusCode(status.value())
                    .build();
        }
        return ExceptionDTO.builder()
                .error(
                        MicroTimeManagementExceptionDTO.builder()
                                .message(ex.getMessage() + " Please contact dev.")
                                .build()
                )
                .code(new Date().getTime())
                .path(request.getRequest().getServletPath())
                .statusCode(status.value())
                .build();
    }

}
