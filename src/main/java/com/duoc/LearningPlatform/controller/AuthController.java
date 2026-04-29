package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.LoginRequest;
import com.duoc.LearningPlatform.dto.LoginResponse;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.security.JwtUtil;
import com.duoc.LearningPlatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token, user));
    }
}
