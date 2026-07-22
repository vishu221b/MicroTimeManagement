package com.microtimemanagement.apiservice.utils;

import com.microtimemanagement.apiservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the currently authenticated user's uid — the ownership key for
 * user-scoped domain data (projects, tasks, links, activities).
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserService userService;

    public String currentUid() {
        return userService.loadUserDTOByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).getUid();
    }
}
