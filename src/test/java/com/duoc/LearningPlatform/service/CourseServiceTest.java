package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.exception.ResourceNotFoundException;
import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.CourseRepository;
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
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void createsCourseWithValidProfessor() {
        User professor = new User("Prof Smith", "smith@example.com", "encodedPassword", Role.PROFESSOR);
        professor.getClass().getDeclaredFields();
        when(userRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.createCourse("Java 101", "Introduction", 1L);

        assertEquals("Java 101", result.getTitle());
        assertEquals(1L, result.getProfessorId());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void throwsWhenCreatingCourseWithInvalidProfessor() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.createCourse("Java 101", "Introduction", 99L);
        });

        assertEquals("Professor not found with id: 99", exception.getMessage());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCreatingCourseWithStudentAsProfessor() {
        User student = new User("Student", "student@example.com", "encodedPassword", Role.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.createCourse("Java 101", "Introduction", 1L);
        });

        assertEquals("User with id 1 is not a professor", exception.getMessage());
    }

    @Test
    void updatesCourseWithValidProfessor() {
        Course existingCourse = new Course("Old Title", "Old Description", 1L);
        User newProfessor = new User("New Prof", "new@example.com", "encodedPassword", Role.PROFESSOR);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(existingCourse));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newProfessor));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.updateCourse(1L, "New Title", "New Description", 2L);

        assertEquals("New Title", result.getTitle());
        assertEquals(2L, result.getProfessorId());
    }

    @Test
    void throwsWhenUpdatingCourseWithInvalidProfessor() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(1L, "New Title", "New Description", 99L);
        });

        assertEquals("Professor not found with id: 99", exception.getMessage());
    }

    @Test
    void findsActiveCourses() {
        Course active1 = new Course("Active 1", "Description", 1L);
        Course active2 = new Course("Active 2", "Description", 1L);
        Course inactive = new Course("Inactive", "Description", 1L);
        inactive.deactivate();

        when(courseRepository.findAll()).thenReturn(List.of(active1, active2, inactive));

        List<Course> result = courseService.findActiveCourses();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Course::isActive));
    }

    @Test
    void findsAllCourses() {
        Course course1 = new Course("Course 1", "Description", 1L);
        Course course2 = new Course("Course 2", "Description", 1L);
        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));

        List<Course> result = courseService.findAllCourses();

        assertEquals(2, result.size());
    }

    @Test
    void getsCourseById() {
        Course course = new Course("Java 101", "Description", 1L);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        Course result = courseService.getCourse(1L);

        assertEquals("Java 101", result.getTitle());
    }

    @Test
    void throwsWhenCourseNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.getCourse(99L);
        });
    }

    @Test
    void deletesCourse() {
        when(courseRepository.existsById(1L)).thenReturn(true);

        courseService.deleteCourse(1L);

        verify(courseRepository).deleteById(1L);
    }

    @Test
    void activatesCourse() {
        Course course = new Course("Java 101", "Description", 1L);
        course.deactivate();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.activateCourse(1L);

        assertTrue(result.isActive());
    }

    @Test
    void deactivatesCourse() {
        Course course = new Course("Java 101", "Description", 1L);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.deactivateCourse(1L);

        assertFalse(result.isActive());
    }
}
