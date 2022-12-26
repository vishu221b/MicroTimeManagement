package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLogoutResponseDTO;
import com.microtimemanagement.apiservice.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    /**
     * Authenticate users - Generate Tokens
     * Verify Tokens
     * Refresh Tokens
     */

    //TODO: Change password

    @Autowired
    AuthService authService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public AuthenticationLoginResponseDTO loginUser(@RequestBody AuthenticationRequestDTO authenticationRequestDTO){
        return authService.generateToken(authenticationRequestDTO);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public AuthenticationLogoutResponseDTO logoutUser(ServletWebRequest request){
        String authHeader = request.getHeader("Authorization");
        return authService.expireToken(authHeader.substring(7));
    }
}
