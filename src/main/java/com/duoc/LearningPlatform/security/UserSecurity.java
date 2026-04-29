package com.duoc.LearningPlatform.security;

import com.duoc.LearningPlatform.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserService userService;

    public UserSecurity(UserService userService) {
        this.userService = userService;
    }

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
}
