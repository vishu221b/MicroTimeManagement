package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiPathConstants;
import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.USER_BASE_ROUTE_V1)
public class UserController {
    /**
     * Create Users - Registration
     * Update Users
     * Delete Users
     * Read Users
     * */

    //TODO: Delete, Read Users, Change Password

    private final UserService userService;

    @RequestMapping(value = ApiPathConstants.REGISTER_USER, method = RequestMethod.POST)
    @ResponseBody
    public NewUserResponseDTO createNewUser(@Valid @RequestBody NewUserRequestDTO requestDTO, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            log.info("Has binding errors: {}", bindingResult.getAllErrors());
            throw new MicroTimeManagementBadRequestException(
                    ErrorConstants.ERROR_ENCOUNTERED_DURING_REQUEST,
                    bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.toList()));
        }else{
            return userService.createNewUser(requestDTO);
        }
    }

    @RequestMapping(value = ApiPathConstants.UPDATE_USER, method = RequestMethod.PUT)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public UserDTO updateUser(@Valid UserDTO userDTO){
        return userService.updateUserDetails(userDTO);
    }

    @RequestMapping(value = ApiPathConstants.DELETE_USER, method = RequestMethod.DELETE)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO deleteUserById(@RequestParam String userId){
        return userService.deleteUserById(userId);
    }

    @RequestMapping(value = ApiPathConstants.RESET_PASSWORD, method = RequestMethod.POST)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO updatePasswordForUser(
            @RequestBody PasswordChangeRequestDTO passwordChangeRequestDTO, Principal principal){
        passwordChangeRequestDTO.setUsername(principal.getName());
        return userService.changeUserPassword(passwordChangeRequestDTO);
    }
}
