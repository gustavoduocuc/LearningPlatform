package com.duoc.LearningPlatform.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void createsUserWithValidData() {
        User user = new User("John Doe", "john@example.com", "password123", Role.STUDENT);

        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(Role.STUDENT, user.getRole());
        // createdAt and updatedAt are set by JPA lifecycle callbacks when persisted
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void rejectsNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User(null, "john@example.com", "password123", Role.STUDENT);
        });
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("   ", "john@example.com", "password123", Role.STUDENT);
        });
    }

    @Test
    void rejectsNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("John Doe", null, "password123", Role.STUDENT);
        });
    }

    @Test
    void rejectsBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("John Doe", "   ", "password123", Role.STUDENT);
        });
    }

    @Test
    void rejectsInvalidEmailFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("John Doe", "invalid-email", "password123", Role.STUDENT);
        });
    }

    @Test
    void rejectsNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("John Doe", "john@example.com", null, Role.STUDENT);
        });
    }

    @Test
    void rejectsBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("John Doe", "john@example.com", "   ", Role.STUDENT);
        });
    }

    @Test
    void rejectsNullRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("John Doe", "john@example.com", "password123", null);
        });
    }

    @Test
    void validatesPasswordCorrectly() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User("John Doe", "john@example.com", encodedPassword, Role.STUDENT);

        assertTrue(user.validatePassword(rawPassword, passwordEncoder));
        assertFalse(user.validatePassword("wrongpassword", passwordEncoder));
    }

    @Test
    void updatesEmailSuccessfully() {
        User user = new User("John Doe", "john@example.com", "password123", Role.STUDENT);

        user.updateEmail("john.doe@example.com");

        assertEquals("john.doe@example.com", user.getEmail());
    }

    @Test
    void rejectsInvalidEmailWhenUpdating() {
        User user = new User("John Doe", "john@example.com", "password123", Role.STUDENT);

        assertThrows(IllegalArgumentException.class, () -> {
            user.updateEmail("invalid-email");
        });
    }

    @Test
    void updatesProfileSuccessfully() {
        User user = new User("John Doe", "john@example.com", "password123", Role.STUDENT);

        user.updateProfile("Jane Doe", "jane@example.com");

        assertEquals("Jane Doe", user.getName());
        assertEquals("jane@example.com", user.getEmail());
    }

    @Test
    void rejectsNullNameWhenUpdatingProfile() {
        User user = new User("John Doe", "john@example.com", "password123", Role.STUDENT);

        assertThrows(IllegalArgumentException.class, () -> {
            user.updateProfile(null, "jane@example.com");
        });
    }

    @Test
    void changesRoleSuccessfully() {
        User user = new User("John Doe", "john@example.com", "password123", Role.STUDENT);

        user.changeRole(Role.PROFESSOR);

        assertEquals(Role.PROFESSOR, user.getRole());
    }

    @Test
    void rejectsNullRoleWhenChanging() {
        User user = new User("John Doe", "john@example.com", "password123", Role.STUDENT);

        assertThrows(IllegalArgumentException.class, () -> {
            user.changeRole(null);
        });
    }

    @Test
    void hasAllRoles() {
        assertNotNull(Role.ADMIN);
        assertNotNull(Role.PROFESSOR);
        assertNotNull(Role.STUDENT);
    }
}
