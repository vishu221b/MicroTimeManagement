package com.microtimemanagement.apiservice.constants;

public class ApiPathConstants {


    public static final String EMPTY_BASE = "/";
    public static final String API_PREFIX_V1 = "/api/v1";
    public static final String USER_BASE_ROUTE_V1 = API_PREFIX_V1 + "/users";
    public static final String ADMIN_BASE_V1 = API_PREFIX_V1 + "/admin";
    public static final String ROLE_BASE = ADMIN_BASE_V1 + "/roles";


    public static final String REGISTER_USER = "/register";
    public static final String UPDATE_USER = "/update";
    public static final String ADD_ROLE_TO_USER = ROLE_BASE + "/addToUser";

    public static final String DELETE_USER = "/delete";
    public static final String RESET_PASSWORD = "/resetPassword";
}
