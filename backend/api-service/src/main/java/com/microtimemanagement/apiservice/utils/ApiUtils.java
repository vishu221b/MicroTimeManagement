package com.microtimemanagement.apiservice.utils;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.stream.Collectors;

@Slf4j
@Component
public class ApiUtils {
    public static void handleValidationErrors(BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            log.info("Has binding errors: {}", bindingResult.getAllErrors());
            throw new MicroTimeManagementBadRequestException(
                    ErrorConstants.PLEASE_FIX_THE_FOLLOWING_ERRORS,
                    bindingResult.getAllErrors()
                            .stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.toList()));
        }
    }

    public static <T> GenericMessageResponseDTO<T> buildResponseDTO(
            String message,
            T t
    ){
        return new GenericMessageResponseDTO<>(t, message);
    }

    public static <T> GenericMessageResponseDTO<T> buildSuccessResponseDTO(T t) {
        return buildResponseDTO(ResponseMessages.SUCCESS,t);
    }
}
