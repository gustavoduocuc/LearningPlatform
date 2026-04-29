package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.User;

public class LoginResponse {

    private String token;
    private UserResponse user;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = UserResponse.fromEntity(user);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
