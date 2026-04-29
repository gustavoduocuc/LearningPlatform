package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.CreateUserRequest;
import com.duoc.LearningPlatform.dto.UpdateUserRequest;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void returnsUsers() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userService.listUsers()).thenReturn(List.of(user));

        ResponseEntity<List<com.duoc.LearningPlatform.dto.UserResponse>> response = userController.listUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getName());
    }

    @Test
    void returnsUserById() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userService.getUser(1L)).thenReturn(user);

        ResponseEntity<com.duoc.LearningPlatform.dto.UserResponse> response = userController.getUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    void createsUser() {
        User user = new User("Jane Doe", "jane@example.com", "encodedPassword", Role.STUDENT);
        when(userService.createUser(any(), any(), any(), any())).thenReturn(user);

        CreateUserRequest request = new CreateUserRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("password123");
        request.setRole(Role.STUDENT);

        ResponseEntity<com.duoc.LearningPlatform.dto.UserResponse> response = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Jane Doe", response.getBody().getName());
    }

    @Test
    void updatesUserFull() {
        User user = new User("Jane Doe", "jane@example.com", "encodedPassword", Role.PROFESSOR);
        when(userService.updateUser(eq(1L), any(), any(), any())).thenReturn(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setRole(Role.PROFESSOR);

        ResponseEntity<com.duoc.LearningPlatform.dto.UserResponse> response = userController.updateUserFull(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Role.PROFESSOR.name(), response.getBody().getRole());
    }

    @Test
    void deletesUser() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }
}
