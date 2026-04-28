package com.microtimemanagement.apiservice.constants;

public class SecurityConstants {

    public static class DEV{
        public static String[] OPEN_API_ENDPOINT_REQUEST_MATCHERS = {
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/actuator/**",
                "/error",
                "/api/v1/user/register",
                "/",
                "/swagger-dev"
        };

        public static String[] SECURE_USER_API_ENDPOINT_REQUEST_MATCHERS = {
                "/api/v1/user/**", "/api/v1/auth/logout"
        };

        public static String[] SECURE_ROLE_API_ENDPOINT_REQUEST_MATCHERS = {
                "/api/v1/role/**"
        };

        public static String[] SECURE_ADMIN_API_ENDPOINT_REQUEST_MATCHERS = {
                "/api/v1/admin/**"
        };

        public static String[] SECURE_ACTIVITY_API_ENDPOINT_REQUEST_MATCHERS = {
                "/api/v1/activity/**"
        };



    }

    public static class PROD{}
}
