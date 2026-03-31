package com.microtimemanagement.apiservice.utils;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthUtils {
    public static Boolean isTokenExpired(Date expiresAt){
        if(null == expiresAt){
            throw new MicroTimeManagementException(ErrorConstants.SESSION_EXPIRY_CANNOT_BE_NULL);
        }
        long currentTime=new Date().getTime();
        if(currentTime > expiresAt.getTime()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
