package com.microtimemanagement.apiservice.utils;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.PaginationConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationRequestFieldsSanitizationResponseDTO;
import com.microtimemanagement.apiservice.enums.ApiResourceType;
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

    public static <T> GenericMessageResponseDTO<T> buildErrorResponseDTO(T t) {
        return buildResponseDTO(ResponseMessages.ERROR,t);
    }

    public static PaginationRequestFieldsSanitizationResponseDTO sanitizePaginationRequestFields(
            Integer pageNumber, Integer pageSize
    ){
        int defaultPageNumber = Integer.parseInt(PaginationConstants.DEFAULT_PAGE_NUMBER);
        int defaultPageSize = Integer.parseInt(PaginationConstants.DEFAULT_PAGE_SIZE);
        int maxPageSize = Integer.parseInt(PaginationConstants.MAX_PAGE_SIZE);

        if(pageNumber == null || pageNumber < defaultPageNumber){
            pageNumber = defaultPageNumber;
        }
        if(pageSize == null || pageSize < 1){
            pageSize = defaultPageSize;
        }
        if(pageSize > maxPageSize){
            pageSize = maxPageSize;
        }
        return PaginationRequestFieldsSanitizationResponseDTO.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }

    public static String getRequestMatcherPatternForBase(String base){
        return base + ApiConstants.SecurityConfig.ANY_REQUEST_MATCHER_SUFFIX;
    }

    public static String buildApiPathForEndpointOfResourceType(String endpoint, ApiResourceType resourceType){
        String base = switch (resourceType) {
            case USER -> ApiConstants.UserEndpoint.API_BASE;
            case ADMIN -> ApiConstants.AdminEndpoint.API_BASE;
            case AUTH -> ApiConstants.AuthEndpoint.API_BASE;
            case ROLE -> ApiConstants.RoleEndpoint.API_BASE;
            case ACTIVITY -> ApiConstants.ActivityEndpoint.API_BASE;
        };
        return base + endpoint;
    }

}
