package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.service.AuthenticationAndAuthorizationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication Operations")
public class AuthenticationController {
    /**
     * Authenticate users - Generate Tokens
     * Verify Tokens
     * Refresh Tokens
     */

    @Autowired
    AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public AuthenticationLoginResponseDTO loginUser(@RequestBody AuthenticationRequestDTO authenticationRequestDTO){
        return authenticationAndAuthorizationService.createNewUserSession(authenticationRequestDTO);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<?> logoutUser(ServletWebRequest request){
        String authHeader = request.getHeader("Authorization");
        if(StringUtils.isNotBlank(authHeader))
            return authenticationAndAuthorizationService.destroyUserSession(authHeader.substring(7));
        throw new MicroTimeManagementNotFoundException("Missing Authorisation Header");
    }

}
