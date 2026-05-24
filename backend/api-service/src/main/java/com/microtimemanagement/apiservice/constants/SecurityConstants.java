package com.microtimemanagement.apiservice.constants;

import com.microtimemanagement.apiservice.enums.ApiResourceType;
import com.microtimemanagement.apiservice.utils.ApiUtils;

public class SecurityConstants {

    public static class DEV{
        public static String[] OPEN_API_ENDPOINT_REQUEST_MATCHERS = {
                ApiUtils.buildApiPathForEndpointOfResourceType(
                        ApiConstants.AuthEndpoint.LOGIN,
                        ApiResourceType.AUTH
                ),
                ApiUtils.buildApiPathForEndpointOfResourceType(
                        ApiConstants.AuthEndpoint.REFRESH,
                        ApiResourceType.AUTH
                ),
                ApiUtils.buildApiPathForEndpointOfResourceType(
                        ApiConstants.UserEndpoint.REGISTER_USER,
                        ApiResourceType.USER
                ),
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/error"
        };

        public static String[] SECURE_USER_API_ENDPOINT_REQUEST_MATCHERS = {
                ApiUtils.getRequestMatcherPatternForBase(
                        ApiConstants.UserEndpoint.API_BASE
                ), ApiUtils.buildApiPathForEndpointOfResourceType(
                        ApiConstants.AuthEndpoint.LOGOUT,
                        ApiResourceType.AUTH
                )
        };

        public static String[] SECURE_ROLE_API_ENDPOINT_REQUEST_MATCHERS = {
                ApiUtils.getRequestMatcherPatternForBase(
                        ApiConstants.RoleEndpoint.API_BASE
                )
        };

        public static String[] SECURE_ADMIN_API_ENDPOINT_REQUEST_MATCHERS = {
                ApiUtils.getRequestMatcherPatternForBase(
                        ApiConstants.AdminEndpoint.API_BASE
                ), ApiUtils.buildApiPathForEndpointOfResourceType(
                        ApiConstants.UserEndpoint.GET_ALL_USERS, ApiResourceType.USER
                ),
                ApiUtils.buildApiPathForEndpointOfResourceType(
                        ApiConstants.UserEndpoint.ADD_ROLE_TO_USER, ApiResourceType.USER
                ),
                ApiUtils.buildApiPathForEndpointOfResourceType(
                        ApiConstants.UserEndpoint.REMOVE_ROLE_FOR_USER, ApiResourceType.USER
                ),
                ApiUtils.buildApiPathForEndpointOfResourceType(
                        ApiConstants.UserEndpoint.GET_USER_BY_UID, ApiResourceType.USER
                ),
                "/actuator/**",
        };

        public static String[] SECURE_ACTIVITY_API_ENDPOINT_REQUEST_MATCHERS = {
                ApiUtils.getRequestMatcherPatternForBase(
                        ApiConstants.ActivityEndpoint.API_BASE
                )
        };



    }

    public static class PROD{}
}
