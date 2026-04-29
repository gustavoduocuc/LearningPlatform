package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.CourseRequest;
import com.duoc.LearningPlatform.dto.CourseResponse;
import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseResponse>> listActiveCourses() {
        List<Course> courses = courseService.findActiveCourses();
        List<CourseResponse> response = courses.stream()
                .map(CourseResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long id) {
        Course course = courseService.getCourse(id);
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        Course course = courseService.createCourse(
                request.getTitle(),
                request.getDescription(),
                request.getProfessorId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseResponse.fromEntity(course));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request
    ) {
        Course course = courseService.updateCourse(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getProfessorId()
        );
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<CourseResponse> activateCourse(@PathVariable Long id) {
        Course course = courseService.activateCourse(id);
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<CourseResponse> deactivateCourse(@PathVariable Long id) {
        Course course = courseService.deactivateCourse(id);
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }
}
