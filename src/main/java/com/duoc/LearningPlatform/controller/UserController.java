package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.CreateUserRequest;
import com.duoc.LearningPlatform.dto.UpdateEmailRequest;
import com.duoc.LearningPlatform.dto.UpdateUserRequest;
import com.duoc.LearningPlatform.dto.UserResponse;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<User> users = userService.listUsers();
        List<UserResponse> response = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.fromEntity(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESSOR') and @userSecurity.isCurrentUser(#id, authentication))")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmailRequest request,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        User user;
        if (isAdmin) {
            throw new IllegalArgumentException("Admin must use full update endpoint");
        } else {
            user = userService.updateUserEmail(id, request.getEmail());
        }

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PutMapping("/{id}/full")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserFull(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = userService.updateUser(
                id,
                request.getName(),
                request.getEmail(),
                request.getRole()
        );
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
