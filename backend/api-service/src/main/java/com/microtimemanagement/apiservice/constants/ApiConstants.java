package com.microtimemanagement.apiservice.constants;

public class ApiConstants {


    public static final String EMPTY_BASE = "/";
    public static final String API_PREFIX_V1 = "/api/v1";

    public static final class UserEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/user";
        public static final String REGISTER_USER = "/register";
        public static final String RESET_PASSWORD = "/resetPassword";
        public static final String UPDATE_USER = "/update";
        public static final String DELETE_CURRENT_USER = "/delete";
        public static final String GET_USER_BY_UID = "/getByUserId";
        public static final String USER_PROFILE = "/profile";
        public static final String ADD_ROLE_TO_USER = "/addRole";
        public static final String REMOVE_ROLE_FOR_USER = "/removeRole";
        public static final String GET_ALL_USERS = "/all";
    }

    public static final class AdminEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/admin";
    }

    public static final class RoleEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/role";
    }

    public static final class ActivityEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/activity";
    }

    public static final class ProjectEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/project";
    }

    public static final class TaskEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/task";
    }

    public static final class LinkEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/link";
    }

    public static final class ReminderEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/reminder";
    }

    public static final class BillingEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/billing";
    }
    public static final class AuthEndpoint {
        public static final String API_BASE = API_PREFIX_V1 + "/auth";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String REFRESH = "/refresh";
    }

    public static final class SecurityConfig{
        public static final String ANY_REQUEST_MATCHER_SUFFIX = EMPTY_BASE + "**";
    }
}
