package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Registration;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.CourseRepository;
import com.duoc.LearningPlatform.repository.RegistrationRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registersStudentSuccessfully() {
        User student = new User("John Doe", "john@example.com", "password", Role.STUDENT);
        Course course = new Course("Java 101", "Description", 1L);
        Registration registration = new Registration(1L, 2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationRepository.existsByCourseIdAndStudentId(1L, 2L)).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenReturn(registration);

        Registration result = registrationService.registerStudent(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getCourseId());
        assertEquals(2L, result.getStudentId());
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    void throwsWhenStudentNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerStudent(1L, 99L);
        });

        assertEquals("Student not found with id: 99", exception.getMessage());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseNotFound() {
        User student = new User("John Doe", "john@example.com", "password", Role.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerStudent(99L, 2L);
        });

        assertEquals("Course not found with id: 99", exception.getMessage());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void throwsWhenUserIsNotStudent() {
        User professor = new User("Prof Smith", "prof@example.com", "password", Role.PROFESSOR);
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerStudent(1L, 2L);
        });

        assertEquals("User with id 2 is not a student", exception.getMessage());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void throwsWhenAlreadyRegistered() {
        User student = new User("John Doe", "john@example.com", "password", Role.STUDENT);
        Course course = new Course("Java 101", "Description", 1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationRepository.existsByCourseIdAndStudentId(1L, 2L)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.registerStudent(1L, 2L);
        });

        assertEquals("Student is already registered for this course", exception.getMessage());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void unregistersStudentSuccessfully() {
        Registration registration = new Registration(1L, 2L);
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
        doNothing().when(registrationRepository).deleteById(1L);

        registrationService.unregisterStudent(1L);

        verify(registrationRepository).deleteById(1L);
    }

    @Test
    void throwsWhenUnregisteringNonExistentRegistration() {
        when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.unregisterStudent(99L);
        });

        assertEquals("Registration not found with id: 99", exception.getMessage());
    }

    @Test
    void findsRegistrationsByCourseId() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(1L, 3L);
        when(registrationRepository.findByCourseId(1L)).thenReturn(List.of(reg1, reg2));

        List<Registration> result = registrationService.findByCourseId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void findsRegistrationsByStudentId() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(3L, 2L);
        when(registrationRepository.findByStudentId(2L)).thenReturn(List.of(reg1, reg2));

        List<Registration> result = registrationService.findByStudentId(2L);

        assertEquals(2, result.size());
    }

    @Test
    void checksIfStudentIsRegisteredForCourse() {
        when(registrationRepository.existsByCourseIdAndStudentId(1L, 2L)).thenReturn(true);
        when(registrationRepository.existsByCourseIdAndStudentId(1L, 99L)).thenReturn(false);

        assertTrue(registrationService.isRegistered(1L, 2L));
        assertFalse(registrationService.isRegistered(1L, 99L));
    }

    @Test
    void findsAllRegistrations() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(3L, 4L);
        when(registrationRepository.findAll()).thenReturn(List.of(reg1, reg2));

        List<Registration> result = registrationService.findAll();

        assertEquals(2, result.size());
    }
}
