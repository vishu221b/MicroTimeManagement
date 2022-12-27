package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication Operations")
public class AuthController {
    /**
     * Authenticate users - Generate Tokens
     * Verify Tokens
     * Refresh Tokens
     */

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
    public GenericMessageResponseDTO logoutUser(ServletWebRequest request){
        String authHeader = request.getHeader("Authorization");
        return authService.expireToken(authHeader.substring(7));
    }

}
