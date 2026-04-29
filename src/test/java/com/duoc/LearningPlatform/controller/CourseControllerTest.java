package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.CourseRequest;
import com.duoc.LearningPlatform.dto.CourseResponse;
import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.service.CourseService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    @Test
    void returnsActiveCourses() {
        Course course = new Course("Java 101", "Introduction to Java", 1L);
        when(courseService.findActiveCourses()).thenReturn(List.of(course));

        ResponseEntity<List<CourseResponse>> response = courseController.listActiveCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Java 101", response.getBody().get(0).getTitle());
        assertEquals(1L, response.getBody().get(0).getProfessorId());
    }

    @Test
    void returnsCourseById() {
        Course course = new Course("Java 101", "Introduction to Java", 1L);
        when(courseService.getCourse(1L)).thenReturn(course);

        ResponseEntity<CourseResponse> response = courseController.getCourse(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Java 101", response.getBody().getTitle());
        assertEquals(1L, response.getBody().getProfessorId());
    }

    @Test
    void createsCourse() {
        Course course = new Course("Java 101", "Introduction to Java", 1L);
        when(courseService.createCourse(any(), any(), any())).thenReturn(course);

        CourseRequest request = new CourseRequest();
        request.setTitle("Java 101");
        request.setDescription("Introduction to Java");
        request.setProfessorId(1L);

        ResponseEntity<CourseResponse> response = courseController.createCourse(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Java 101", response.getBody().getTitle());
    }

    @Test
    void updatesCourse() {
        Course course = new Course("Updated Java", "Updated Description", 2L);
        when(courseService.updateCourse(eq(1L), any(), any(), any())).thenReturn(course);

        CourseRequest request = new CourseRequest();
        request.setTitle("Updated Java");
        request.setDescription("Updated Description");
        request.setProfessorId(2L);

        ResponseEntity<CourseResponse> response = courseController.updateCourse(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Java", response.getBody().getTitle());
        assertEquals(2L, response.getBody().getProfessorId());
    }

    @Test
    void deletesCourse() {
        ResponseEntity<Void> response = courseController.deleteCourse(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void activatesCourse() {
        Course course = new Course("Java 101", "Description", 1L);
        when(courseService.activateCourse(1L)).thenReturn(course);

        ResponseEntity<CourseResponse> response = courseController.activateCourse(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isActive());
    }

    @Test
    void deactivatesCourse() {
        Course course = new Course("Java 101", "Description", 1L);
        course.deactivate();
        when(courseService.deactivateCourse(1L)).thenReturn(course);

        ResponseEntity<CourseResponse> response = courseController.deactivateCourse(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isActive());
    }
}
