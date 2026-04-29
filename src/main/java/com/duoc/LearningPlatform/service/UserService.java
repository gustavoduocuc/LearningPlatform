package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String name, String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(name, email, encodedPassword, role);
        return userRepository.save(user);
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.validatePassword(password, passwordEncoder)) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return user;
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUserEmail(Long id, String email) {
        User user = getUser(id);

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.updateEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, String name, String email, Role role) {
        User user = getUser(id);

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.updateProfile(name, email);
        user.changeRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isProfessor(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == Role.PROFESSOR)
                .orElse(false);
    }
}
