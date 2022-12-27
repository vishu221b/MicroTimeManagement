package com.microtimemanagement.apiservice.constants;

public class ErrorConstants {
    public static final String INVALID_DATE_VALUE = "Invalid date value. Please supply date in correct format.";
    public static final String OVERLAPPING_NEW_ACTIVITY_TIME_WITH_PREVIOUS_ACTIVITY = "Overlapping activity time. Cannot create record as a record already exists with time falling in between the provided time span.";
    public static final String ROLE_NOT_FOUND_ERROR = "No role found.";
    
    public static final String ROLE_NOT_FOUND_WTH_NAME_ERROR = "No role found with name %s.";
    public static final String ACTIVE_ROLE_ALREADY_EXISTS_ERROR = "An active role already exists with the same name.";
    public static final String USER_ALREADY_EXISTS_FOR_USERNAME = "User with the same username already exists!";
    public static final String USER_ALREADY_EXISTS_FOR_EMAIL = "User with the same email already exists!";
    public static final String ROLE_NAME_SHOULD_NOT_START_WITH_ROLE = "Role name should not start with 'ROLE' or 'ROLE_'";
    public static final String INVALID_USER_IDENTIFIER = "Invalid user identifier. Please provide a valid user identifier.";
    public static final String USER_NOT_FOUND_FOR_IDENTIFIER = "No user found for identifier: %s";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong. Please contact dev.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String EMAIL_NOT_VALID = "Email is not valid.";
    public static final String ERROR_ENCOUNTERED_DURING_REQUEST = "Error encountered in request.";
    public static final String NO_USER_FOUND_FOR_UPDATE = "No user found for update.";
    public static final String CANNOT_UPDATE_ID_OF_EXISTING_USER = "Cannot update id for an existing user.";
    public static final String CANNOT_UPDATE_UID_OF_EXISTING_USER = "Cannot update uid for an existing user.";
    public static final String ACCOUNT_NOT_FOUND_FOR_USERNAME = "No account found for username: %s";
    public static final String SESSION_EXPIRED = "Your session has expired. Please login again.";
    public static final String SESSION_TOKEN_INVALID = "Invalid session token.";
    public static final String ACTIVITY_NOT_FOUND = "No activity found.";
}
