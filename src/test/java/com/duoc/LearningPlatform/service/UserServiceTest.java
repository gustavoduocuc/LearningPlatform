package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createsUserWithEncodedPassword() {
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User user = i.getArgument(0);
            user.getClass().getDeclaredFields();
            return user;
        });

        User result = userService.createUser("John Doe", "john@example.com", "password123", Role.STUDENT);

        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("John Doe", "john@example.com", "password123", Role.STUDENT);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticatesWithValidCredentials() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        User result = userService.authenticate("john@example.com", "password123");

        assertEquals("John Doe", result.getName());
        assertEquals(Role.STUDENT, result.getRole());
    }

    @Test
    void throwsWhenEmailNotFoundForAuthentication() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.authenticate("nonexistent@example.com", "password123");
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void throwsWhenPasswordInvalidForAuthentication() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.authenticate("john@example.com", "wrongpassword");
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void getsUserById() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUser(1L);

        assertEquals("John Doe", result.getName());
    }

    @Test
    void throwsWhenUserNotFoundById() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.getUser(99L);
        });
    }

    @Test
    void listsAllUsers() {
        User user1 = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        User user2 = new User("Jane Doe", "jane@example.com", "encodedPassword", Role.PROFESSOR);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.listUsers();

        assertEquals(2, result.size());
    }

    @Test
    void updatesUserEmail() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUserEmail(1L, "newemail@example.com");

        assertEquals("newemail@example.com", result.getEmail());
    }

    @Test
    void throwsWhenUpdatingToExistingEmail() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserEmail(1L, "existing@example.com");
        });

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void updatesUserFullyAsAdmin() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(1L, "Jane Doe", "jane@example.com", Role.PROFESSOR);

        assertEquals("Jane Doe", result.getName());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals(Role.PROFESSOR, result.getRole());
    }

    @Test
    void deletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void throwsWhenDeletingNonexistentUser() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(99L);
        });
    }

    @Test
    void findsUserByEmail() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void checksIfProfessorExists() {
        User professor = new User("Prof Smith", "smith@example.com", "encodedPassword", Role.PROFESSOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(professor));

        boolean result = userService.isProfessor(1L);

        assertTrue(result);
    }

    @Test
    void returnsFalseWhenUserIsNotProfessor() {
        User student = new User("Student", "student@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        boolean result = userService.isProfessor(1L);

        assertFalse(result);
    }
}
