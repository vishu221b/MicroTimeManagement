package com.microtimemanagement.apiservice.factories;

import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.model.User;

import java.util.Set;
import java.util.UUID;

public class UserTestFactory {

    public static class MtmAppUserAttributes {

        public static String USERNAME = "MTMTestUserUsername";
        public static String EMAIL = "MTMTestUserEmail";
        public static String FIRST_NAME = "MTMTestUserFirstName";
        public static String LAST_NAME = "MTMTestUserLastName";
        public static String DATE_OF_BIRTH = "12-12-2002";
        public static String PASSWORD = "MTMUserTestPassword";
        public static String BCRYPT_PASSWORD_RANDOM_HASH = "MTMUserTestPasswordBcryptHash";

        public static Set<String> DEFAULT_USER_ROLE_NAMES = Set.of(
                RoleTestFactory.MtmRoleNames.ACTIVITY_OPS,
                RoleTestFactory.MtmRoleNames.USER_OPS
        );

        public static Set<String> DEFAULT_USER_ROLE_IDS = Set.of(
                RoleTestFactory.MtmRoleIds.ACTIVITY_OPS,
                RoleTestFactory.MtmRoleIds.USER_OPS
        );
    }

    public static class MtmAdminUserAttributes {

        public static String USERNAME = "MTMTestAdminUserUsername";
        public static String EMAIL = "MTMTestAdminUserEmail";
        public static String FIRST_NAME = "MTMTestAdminUserFirstName";
        public static String LAST_NAME = "MTMTestAdminUserLastName";
        public static String DATE_OF_BIRTH = "12-12-1998";
        public static String PASSWORD = "MTMAdminUserTestPassword";
        public static String BCRYPT_PASSWORD_RANDOM_HASH = "MTMAdminUserTestPasswordBcryptHash";

        public static Set<String> DEFAULT_ADMIN_USER_ROLE_NAMES = Set.of(
                RoleTestFactory.MtmRoleNames.ACTIVITY_OPS,
                RoleTestFactory.MtmRoleNames.USER_OPS,
                RoleTestFactory.MtmRoleNames.ROLE_OPS,
                RoleTestFactory.MtmRoleNames.ADMIN_OPS
        );

        public static Set<String> DEFAULT_ADMIN_USER_ROLE_IDS = Set.of(
                RoleTestFactory.MtmRoleIds.ACTIVITY_OPS,
                RoleTestFactory.MtmRoleIds.USER_OPS,
                RoleTestFactory.MtmRoleIds.ROLE_OPS,
                RoleTestFactory.MtmRoleIds.ADMIN_OPS
        );
    }

    public static NewUserRequestDTO getNewUserRequestDTO(){
        return NewUserRequestDTO.builder()
                .firstName("Test First")
                .lastName("Test Last")
                .email("test@test.com")
                .dateOfBirth("12-12-2002")
                .username("testersFirst")
                .password("Test123")
                .build();
    }

    public static User.UserBuilder<?,?> existingAppUserEntity(){
        return User.builder()
                .firstName(MtmAppUserAttributes.FIRST_NAME)
                .lastName(MtmAppUserAttributes.LAST_NAME)
                .email(MtmAppUserAttributes.EMAIL)
                .dateOfBirth(MtmAppUserAttributes.DATE_OF_BIRTH)
                .username(MtmAppUserAttributes.USERNAME)
                .password(MtmAppUserAttributes.BCRYPT_PASSWORD_RANDOM_HASH)
                .roles(MtmAppUserAttributes.DEFAULT_USER_ROLE_IDS)
                .uid(UUID.randomUUID().toString());
    }

    public static UserDTO.UserDTOBuilder existingAppUserDTO(){
        return UserDTO.builder()
                .firstName(MtmAppUserAttributes.FIRST_NAME)
                .lastName(MtmAppUserAttributes.LAST_NAME)
                .email(MtmAppUserAttributes.EMAIL)
                .dateOfBirth(MtmAppUserAttributes.DATE_OF_BIRTH)
                .username(MtmAppUserAttributes.USERNAME)
                .password(MtmAppUserAttributes.BCRYPT_PASSWORD_RANDOM_HASH)
                .roles(MtmAppUserAttributes.DEFAULT_USER_ROLE_NAMES)
                .uid(UUID.randomUUID().toString());
    }

    public static User.UserBuilder<?,?> adminUserEntity(){
        return User.builder()
                .firstName(MtmAdminUserAttributes.FIRST_NAME)
                .lastName(MtmAdminUserAttributes.LAST_NAME)
                .email(MtmAdminUserAttributes.EMAIL)
                .dateOfBirth(MtmAdminUserAttributes.DATE_OF_BIRTH)
                .username(MtmAdminUserAttributes.USERNAME)
                .password(MtmAdminUserAttributes.BCRYPT_PASSWORD_RANDOM_HASH)
                .uid(UUID.randomUUID().toString())
                .roles(MtmAdminUserAttributes.DEFAULT_ADMIN_USER_ROLE_IDS);
    }

}
