package com.duoc.LearningPlatform.security;

import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("mySecretKeyForTestingPurposesOnly12345678901234567890");
        testUser = new User("John Doe", "john@example.com", "password123", Role.STUDENT);
    }

    @Test
    void generatesValidToken() {
        String token = jwtUtil.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validatesTokenSuccessfully() {
        String token = jwtUtil.generateToken(testUser);

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void rejectsInvalidToken() {
        boolean isValid = jwtUtil.validateToken("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    void rejectsMalformedToken() {
        boolean isValid = jwtUtil.validateToken("not-a-valid-jwt");

        assertFalse(isValid);
    }

    @Test
    void extractsUserIdFromToken() {
        String token = jwtUtil.generateToken(testUser);

        Long userId = jwtUtil.extractUserId(token);

        assertNull(userId);
    }

    @Test
    void extractsEmailFromToken() {
        String token = jwtUtil.generateToken(testUser);

        String email = jwtUtil.extractEmail(token);

        assertEquals("john@example.com", email);
    }

    @Test
    void extractsRoleFromToken() {
        String token = jwtUtil.generateToken(testUser);

        String role = jwtUtil.extractRole(token);

        assertEquals("STUDENT", role);
    }

    @Test
    void returnsNullForInvalidTokenExtraction() {
        assertNull(jwtUtil.extractEmail("invalid.token"));
        assertNull(jwtUtil.extractRole("invalid.token"));
    }

    @Test
    void generatesTokenWithDifferentRoles() {
        User admin = new User("Admin", "admin@example.com", "password123", Role.ADMIN);
        User professor = new User("Prof", "prof@example.com", "password123", Role.PROFESSOR);

        String adminToken = jwtUtil.generateToken(admin);
        String professorToken = jwtUtil.generateToken(professor);

        assertEquals("ADMIN", jwtUtil.extractRole(adminToken));
        assertEquals("PROFESSOR", jwtUtil.extractRole(professorToken));
    }
}
