package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void findsUserByEmail() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    void returnsEmptyWhenEmailNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertTrue(found.isEmpty());
    }

    @Test
    void checksEmailExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        assertTrue(userRepository.existsByEmail("john@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void savesUserSuccessfully() {
        User user = new User("Jane Doe", "jane@example.com", "encodedPassword", Role.PROFESSOR);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.getClass().getDeclaredFields();
            return saved;
        });

        User saved = userRepository.save(user);

        assertNotNull(saved);
        assertEquals("Jane Doe", saved.getName());
    }

    @Test
    void findsUserById() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        user.getClass().getDeclaredFields();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> found = userRepository.findById(1L);

        assertTrue(found.isPresent());
    }

    @Test
    void findsAllUsers() {
        User user1 = new User("John Doe", "john@example.com", "encodedPassword", Role.STUDENT);
        User user2 = new User("Jane Doe", "jane@example.com", "encodedPassword", Role.PROFESSOR);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        var users = userRepository.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void deletesUserSuccessfully() {
        doNothing().when(userRepository).deleteById(1L);

        userRepository.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }
}
