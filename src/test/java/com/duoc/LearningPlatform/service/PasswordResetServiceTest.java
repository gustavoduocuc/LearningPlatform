package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.PasswordResetCode;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.PasswordResetCodeRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetOtpNotifier notifier;

    private PasswordResetService passwordResetService;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneId.systemDefault());
        passwordResetService = createServiceWith(fixedClock);
    }

    private PasswordResetService createServiceWith(Clock clock) {
        int otpTtlMinutes = 15;
        return new PasswordResetService(
                userRepository,
                passwordResetCodeRepository,
                passwordEncoder,
                notifier,
                clock,
                otpTtlMinutes
        );
    }

    @Test
    void sendsResetCodeToExistingUser() {
        String email = "john@example.com";
        User user = new User("John Doe", email, "oldEncoded", Role.STUDENT);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("hashedOtp123");
        when(passwordResetCodeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = passwordResetService.requestOtp(email);

        assertEquals("If an account exists for this email, password reset instructions will be sent.", result);
        verify(passwordResetCodeRepository).deleteByEmail(email);
        verify(passwordEncoder).encode(any());
        ArgumentCaptor<PasswordResetCode> codeCaptor = ArgumentCaptor.forClass(PasswordResetCode.class);
        verify(passwordResetCodeRepository).save(codeCaptor.capture());
        assertEquals(email, codeCaptor.getValue().getEmail());
        assertEquals("hashedOtp123", codeCaptor.getValue().getCodeHash());
        assertEquals(LocalDateTime.now(fixedClock).plusMinutes(15), codeCaptor.getValue().getExpiresAt());
        verify(notifier).deliverOtp(any(), any());
    }

    @Test
    void doesNotSendCodeForUnknownEmail() {
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        String result = passwordResetService.requestOtp(email);

        assertEquals("If an account exists for this email, password reset instructions will be sent.", result);
        verify(passwordResetCodeRepository, never()).deleteByEmail(any());
        verify(passwordResetCodeRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
        verify(notifier, never()).deliverOtp(any(), any());
    }

    @Test
    void resetsPasswordWithValidCode() {
        String email = "john@example.com";
        String plainOtp = "123456";
        String newPassword = "newPassword123";
        LocalDateTime expiry = LocalDateTime.now(fixedClock).plusMinutes(10);
        PasswordResetCode code = new PasswordResetCode(email, "hashedOtp", expiry);
        User user = new User("John Doe", email, "oldEncoded", Role.STUDENT);

        when(passwordResetCodeRepository.findByEmail(email)).thenReturn(Optional.of(code));
        when(passwordEncoder.matches(plainOtp, "hashedOtp")).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordResetCodeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = passwordResetService.resetPassword(email, plainOtp, newPassword);

        assertEquals("Password has been reset successfully.", result);
        assertTrue(code.isConsumed());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
        verify(passwordResetCodeRepository).save(code);
    }

    @Test
    void rejectsResetWithWrongCode() {
        String email = "john@example.com";
        String wrongOtp = "wrongOtp";
        String newPassword = "newPassword123";
        LocalDateTime expiry = LocalDateTime.now(fixedClock).plusMinutes(10);
        PasswordResetCode code = new PasswordResetCode(email, "hashedOtp", expiry);

        when(passwordResetCodeRepository.findByEmail(email)).thenReturn(Optional.of(code));
        when(passwordEncoder.matches(wrongOtp, "hashedOtp")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.resetPassword(email, wrongOtp, newPassword);
        });

        assertEquals("Invalid or expired verification code.", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsResetWithExpiredCode() {
        String email = "john@example.com";
        String plainOtp = "123456";
        String newPassword = "newPassword123";
        Clock laterClock = Clock.fixed(Instant.parse("2026-01-01T12:20:00Z"), ZoneId.systemDefault());
        passwordResetService = createServiceWith(laterClock);
        LocalDateTime pastExpiry = LocalDateTime.now(laterClock).minusMinutes(5);
        PasswordResetCode code = new PasswordResetCode(email, "hashedOtp", pastExpiry);

        when(passwordResetCodeRepository.findByEmail(email)).thenReturn(Optional.of(code));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.resetPassword(email, plainOtp, newPassword);
        });

        assertEquals("Invalid or expired verification code.", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsResetWithConsumedCode() {
        String email = "john@example.com";
        String plainOtp = "123456";
        String newPassword = "newPassword123";
        LocalDateTime expiry = LocalDateTime.now(fixedClock).plusMinutes(10);
        LocalDateTime consumedAt = LocalDateTime.now(fixedClock).minusMinutes(5);
        PasswordResetCode code = new PasswordResetCode(email, "hashedOtp", expiry);
        code.markConsumed(consumedAt);

        when(passwordResetCodeRepository.findByEmail(email)).thenReturn(Optional.of(code));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.resetPassword(email, plainOtp, newPassword);
        });

        assertEquals("Invalid or expired verification code.", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsResetWhenUserNotFound() {
        String email = "john@example.com";
        String plainOtp = "123456";
        String newPassword = "newPassword123";
        LocalDateTime expiry = LocalDateTime.now(fixedClock).plusMinutes(10);
        PasswordResetCode code = new PasswordResetCode(email, "hashedOtp", expiry);

        when(passwordResetCodeRepository.findByEmail(email)).thenReturn(Optional.of(code));
        when(passwordEncoder.matches(plainOtp, "hashedOtp")).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.resetPassword(email, plainOtp, newPassword);
        });

        assertEquals("Invalid or expired verification code.", exception.getMessage());
    }

    @Test
    void rejectsResetWithBlankPassword() {
        String email = "john@example.com";
        String plainOtp = "123456";
        String blankPassword = "   ";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.resetPassword(email, plainOtp, blankPassword);
        });

        assertEquals("Password cannot be empty", exception.getMessage());
        verify(passwordResetCodeRepository, never()).findByEmail(any());
    }

    @Test
    void rejectsResetWithNullPassword() {
        String email = "john@example.com";
        String plainOtp = "123456";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.resetPassword(email, plainOtp, null);
        });

        assertEquals("Password cannot be empty", exception.getMessage());
        verify(passwordResetCodeRepository, never()).findByEmail(any());
    }
}
