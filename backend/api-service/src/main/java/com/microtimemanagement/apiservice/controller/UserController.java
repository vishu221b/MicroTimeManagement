package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.ApiPathConstants;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserDetailsUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.service.UserService;
import com.microtimemanagement.apiservice.utils.ApiUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

//@CrossOrigin(maxAge = 3600, originPatterns = {"*"})
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.USER_BASE_ROUTE_V1)
@Tag(name = "Users", description = "User Operations")
public class UserController {
    /**
     * Create Users - Registration
     * Update Users
     * Delete Users
     * Read User by UID
     * Get User Profile
     * Update Password
     * */

    private final UserService userService;

    /**
     * Creates a new user
     * */
    @RequestMapping(value = ApiPathConstants.REGISTER_USER, method = RequestMethod.POST)
    @ResponseBody
    public GenericMessageResponseDTO<?> createNewUser(
            @Valid @RequestBody NewUserRequestDTO requestDTO,
            BindingResult bindingResult
    ){
        log.info("Received request: {}", requestDTO);
        ApiUtils.handleValidationErrors(bindingResult);
        return userService.createNewUser(requestDTO);
    }

    /**
     * Updates the currently logged-in user details
     * */
    @RequestMapping(value = ApiPathConstants.UPDATE_USER, method = RequestMethod.PUT)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<?> updateUser(
            @Valid UserDetailsUpdateRequestDTO userDetailsUpdateRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return userService.updateUserDetails(userDetailsUpdateRequestDTO);
    }

    /**
     * Deletes the currently logged-in user
     * */
    @RequestMapping(value = ApiPathConstants.DELETE_CURRENT_USER, method = RequestMethod.DELETE)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<?> deleteUserById(Principal principal){
        return userService.deleteUserByUsername(principal.getName());
    }

    /**
     * Resets password for the currently logged-in user
     * */
    @RequestMapping(value = ApiPathConstants.RESET_PASSWORD, method = RequestMethod.POST)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<?> updatePasswordForUser(
            @RequestBody @Valid PasswordChangeRequestDTO passwordChangeRequestDTO,
            BindingResult bindingResult,
            Principal principal){
        ApiUtils.handleValidationErrors(bindingResult);
        passwordChangeRequestDTO.setUsername(principal.getName());
        return userService.changeUserPassword(passwordChangeRequestDTO);
    }

    /**
     * Gets user details for any user by id
     * Ideally should be accessible by Admin only
     * */
    @RequestMapping(value = ApiPathConstants.GET_USER_BY_UID, method = RequestMethod.GET)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public UserDTO getUserByUid(@RequestParam String id){
        return userService.getUserByUid(id);
    }

    /**
     * Get the currently logged-in user's profile
     *
     * @param user
     * @return Profile for the currently signed-in user
     */
    @RequestMapping(value = ApiPathConstants.USER_PROFILE, method = RequestMethod.GET)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public UserDTO getUserProfile(@AuthenticationPrincipal User user){
        return userService.getUserProfile(user);
    }
}
