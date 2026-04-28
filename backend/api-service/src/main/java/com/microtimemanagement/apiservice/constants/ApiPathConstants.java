package com.microtimemanagement.apiservice.constants;

public class ApiPathConstants {


    public static final String EMPTY_BASE = "/";
    public static final String API_PREFIX_V1 = "/api/v1";
    public static final String USER_BASE_ENDPOINT = API_PREFIX_V1 + "/user";
    public static final String ADMIN_BASE_ENDPOINT = API_PREFIX_V1 + "/admin";
    public static final String ROLE_BASE_ENDPOINT = API_PREFIX_V1 + "/role";
    public static final String ACTIVITY_BASE_ENDPOINT = API_PREFIX_V1 + "/activity";
    public static final String AUTH_BASE_ENDPOINT = API_PREFIX_V1 + "/auth";

    public static final String REGISTER_USER = "/register";
    public static final String RESET_PASSWORD = "/resetPassword";
    public static final String UPDATE_USER = "/update";
    public static final String DELETE_CURRENT_USER = "/delete";
    public static final String GET_USER_BY_UID = "/getByUserId";
    public static final String USER_PROFILE = "/profile";

    public static final String ADD_ROLE_TO_USER = "/addToUser";
    public static final String REMOVE_ROLE_FOR_USER = "/removeForUser";
}
