package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.constants.*;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserDetailsUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UsersRolesUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationRequestFieldsSanitizationResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationResultResponseDTO;
import com.microtimemanagement.apiservice.enums.UserRoleUpdateAction;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.service.UserService;
import com.microtimemanagement.apiservice.utils.ApiUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

//@CrossOrigin(maxAge = 3600, originPatterns = {"*"})
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.UserEndpoint.API_BASE)
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
    @RequestMapping(value = ApiConstants.UserEndpoint.REGISTER_USER, method = RequestMethod.POST)
    @ResponseBody
    public GenericMessageResponseDTO<NewUserResponseDTO> createNewUser(
            @Valid @RequestBody NewUserRequestDTO requestDTO,
            BindingResult bindingResult
    ){
        log.info("Received request: {}", requestDTO);
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildResponseDTO(
                ResponseMessages.USER_REGISTRATION_SUCCESS,
                userService.createNewUser(requestDTO)
        );
    }

    /**
     * Updates the currently logged-in user details
     * */
    @RequestMapping(value = ApiConstants.UserEndpoint.UPDATE_USER, method = RequestMethod.PUT)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<UserDTO> updateUser(
            @Valid @RequestBody UserDetailsUpdateRequestDTO userDetailsUpdateRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildResponseDTO(
                ResponseMessages.USER_DETAILS_UPDATED,
                userService.updateUserDetails(userDetailsUpdateRequestDTO)
        );
    }

    /**
     * Deletes the currently logged-in user
     * */
    @RequestMapping(value = ApiConstants.UserEndpoint.DELETE_CURRENT_USER, method = RequestMethod.DELETE)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<String> deleteUserById(Principal principal){
        return ApiUtils.buildSuccessResponseDTO(userService.deleteUserByUsername(principal.getName())
        );
    }

    /**
     * Resets password for the currently logged-in user
     * */
    @RequestMapping(value = ApiConstants.UserEndpoint.RESET_PASSWORD, method = RequestMethod.POST)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<String> updatePasswordForUser(
            @RequestBody @Valid PasswordChangeRequestDTO passwordChangeRequestDTO,
            BindingResult bindingResult,
            Principal principal){
        ApiUtils.handleValidationErrors(bindingResult);
        passwordChangeRequestDTO.setUsername(principal.getName());
        return ApiUtils.buildSuccessResponseDTO(
                userService.changeUserPassword(passwordChangeRequestDTO)
        );
    }

    /**
     * Gets user details for any user by id. Admin-only.
     * */
    @RequestMapping(value = ApiConstants.UserEndpoint.GET_USER_BY_UID, method = RequestMethod.GET)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<UserDTO> getUserByUid(@RequestParam String id){
        return ApiUtils.buildSuccessResponseDTO(userService.getUserDTOByUid(id));
    }

    /**
     * Get the currently logged-in user's profile
     *
     * @param user
     * @return Profile for the currently signed-in user
     */
    @RequestMapping(value = ApiConstants.UserEndpoint.USER_PROFILE, method = RequestMethod.GET)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<UserDTO> getUserProfile(@AuthenticationPrincipal User user){
        return ApiUtils.buildSuccessResponseDTO(userService.getUserProfile(user));
    }

    @GetMapping(value = ApiConstants.UserEndpoint.GET_ALL_USERS)
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<PaginationResultResponseDTO<UserDTO>> getAllUsers(
            @RequestParam(name = "page", required = false, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = PaginationConstants.DEFAULT_SORTING_DIRECTION) String sort,
            @RequestParam(name = "sortBy", required = false, defaultValue = PaginationConstants.DEFAULT_FIELD_TO_SORT_BY) String... sortByFields
    ){
        PaginationRequestFieldsSanitizationResponseDTO sanitizationResponseDTO = ApiUtils
                .sanitizePaginationRequestFields(pageNumber, pageSize);
        PageRequest pageRequest = PageRequest.of(
                sanitizationResponseDTO.getPageNumber(),
                sanitizationResponseDTO.getPageSize(),
                Sort.Direction.fromString(sort),
                sortByFields
        );
        return ApiUtils.buildSuccessResponseDTO(
                userService.getAllUsers(pageRequest)
        );
    }

    @PostMapping(value = ApiConstants.UserEndpoint.ADD_ROLE_TO_USER)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<List<UserDTO>> addRolesToUsers(
            @RequestBody @Valid UsersRolesUpdateRequestDTO usersRolesUpdateRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildSuccessResponseDTO(
                userService.modifyUserRoles(usersRolesUpdateRequestDTO, UserRoleUpdateAction.ADD)
        );
    }

    @DeleteMapping(value = ApiConstants.UserEndpoint.REMOVE_ROLE_FOR_USER)
    @ResponseBody
    @SecurityRequirement(name = "MTM Auth")
    public GenericMessageResponseDTO<List<UserDTO>> removeRolesFromUsers(
            @Valid @RequestBody UsersRolesUpdateRequestDTO usersRolesUpdateRequestDTO,
            BindingResult bindingResult
    ){
        ApiUtils.handleValidationErrors(bindingResult);
        return ApiUtils.buildSuccessResponseDTO(
                userService.modifyUserRoles(usersRolesUpdateRequestDTO, UserRoleUpdateAction.REMOVE)
        );
    }
}
