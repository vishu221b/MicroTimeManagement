package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.dto.request.AccessTokenRefreshRequestDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.service.AuthenticationAndAuthorizationService;
import com.microtimemanagement.apiservice.utils.ApiUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
@RequestMapping(ApiConstants.AuthEndpoint.API_BASE)
@Tag(name = "Authentication", description = "Authentication Operations")
@RequiredArgsConstructor
public class AuthenticationController {
    /**
     * Authenticate users - Generate Tokens
     * Verify Tokens
     * Refresh Tokens
     */


    private final AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public GenericMessageResponseDTO<AuthenticationLoginResponseDTO> loginUser(
            @Valid @RequestBody AuthenticationRequestDTO authenticationRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildResponseDTO(
                ResponseMessages.SUCCESS,
                authenticationAndAuthorizationService.microTimeManagementSessionLogin(authenticationRequestDTO)
        );
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    public GenericMessageResponseDTO<AuthenticationLoginResponseDTO> refreshSession(
            @Valid  @RequestBody AccessTokenRefreshRequestDTO accessTokenRefreshRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildResponseDTO(
                ResponseMessages.SUCCESS,
                authenticationAndAuthorizationService.microTimeManagementSessionRefresh(accessTokenRefreshRequestDTO)
        );
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<String> logoutUser(ServletWebRequest request){
        String authHeader = request.getHeader("Authorization");
        if(StringUtils.isNotBlank(authHeader)){
            authenticationAndAuthorizationService.microTimeManagementSessionLogout(authHeader.substring(7));
            return ApiUtils.buildResponseDTO(ResponseMessages.LOGOUT_SUCCESS, ResponseMessages.SUCCESS);
        }
        throw new MicroTimeManagementNotFoundException("Missing Authorisation Header");
    }

}
