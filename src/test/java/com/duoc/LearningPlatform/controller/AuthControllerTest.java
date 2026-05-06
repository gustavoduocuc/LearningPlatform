package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.security.JwtUtil;
import com.duoc.LearningPlatform.service.PasswordResetService;
import com.duoc.LearningPlatform.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginWithValidCredentialsReturnsToken() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userService.authenticate("john@example.com", "password123")).thenReturn(user);
        when(jwtUtil.generateToken(user)).thenReturn("mocked-jwt-token");

        com.duoc.LearningPlatform.dto.LoginRequest request = new com.duoc.LearningPlatform.dto.LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        ResponseEntity<com.duoc.LearningPlatform.dto.LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mocked-jwt-token", response.getBody().getToken());
        assertEquals("john@example.com", response.getBody().getUser().getEmail());
    }

    @Test
    void loginWithInvalidCredentialsThrowsException() {
        when(userService.authenticate(any(), any())).thenThrow(new BadCredentialsException("Invalid email or password"));

        com.duoc.LearningPlatform.dto.LoginRequest request = new com.duoc.LearningPlatform.dto.LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("wrongpassword");

        assertThrows(BadCredentialsException.class, () -> authController.login(request));
    }

    @Test
    void forgotPasswordReturnsSuccessMessage() {
        String expectedMessage = "If an account exists for this email, password reset instructions will be sent.";
        when(passwordResetService.requestOtp("john@example.com")).thenReturn(expectedMessage);

        com.duoc.LearningPlatform.dto.ForgotPasswordRequest request = new com.duoc.LearningPlatform.dto.ForgotPasswordRequest();
        request.setEmail("john@example.com");

        ResponseEntity<com.duoc.LearningPlatform.dto.MessageResponse> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().getMessage());
    }

    @Test
    void forgotPasswordReturnsSameMessageForUnknownEmail() {
        String expectedMessage = "If an account exists for this email, password reset instructions will be sent.";
        when(passwordResetService.requestOtp("unknown@example.com")).thenReturn(expectedMessage);

        com.duoc.LearningPlatform.dto.ForgotPasswordRequest request = new com.duoc.LearningPlatform.dto.ForgotPasswordRequest();
        request.setEmail("unknown@example.com");

        ResponseEntity<com.duoc.LearningPlatform.dto.MessageResponse> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().getMessage());
    }

    @Test
    void resetPasswordReturnsSuccessMessage() {
        String expectedMessage = "Password has been reset successfully.";
        when(passwordResetService.resetPassword("john@example.com", "123456", "newPassword123"))
                .thenReturn(expectedMessage);

        com.duoc.LearningPlatform.dto.ResetPasswordRequest request = new com.duoc.LearningPlatform.dto.ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPassword123");

        ResponseEntity<com.duoc.LearningPlatform.dto.MessageResponse> response = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().getMessage());
    }

    @Test
    void resetPasswordThrowsExceptionForInvalidOtp() {
        when(passwordResetService.resetPassword(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid or expired verification code."));

        com.duoc.LearningPlatform.dto.ResetPasswordRequest request = new com.duoc.LearningPlatform.dto.ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setOtp("wrongOtp");
        request.setNewPassword("newPassword123");

        assertThrows(IllegalArgumentException.class, () -> authController.resetPassword(request));
    }
}
