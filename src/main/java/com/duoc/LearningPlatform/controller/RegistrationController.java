package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.RegistrationRequest;
import com.duoc.LearningPlatform.dto.RegistrationResponse;
import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Registration;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.CourseService;
import com.duoc.LearningPlatform.service.RegistrationService;
import com.duoc.LearningPlatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final CourseService courseService;
    private final UserService userService;

    public RegistrationController(RegistrationService registrationService,
                                   CourseService courseService,
                                   UserService userService) {
        this.registrationService = registrationService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RegistrationResponse>> listRegistrations(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId) {
        List<Registration> registrations;
        if (courseId != null) {
            registrations = registrationService.findByCourseId(courseId);
        } else if (studentId != null) {
            registrations = registrationService.findByStudentId(studentId);
        } else {
            registrations = registrationService.findAll();
        }

        List<RegistrationResponse> response = registrations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponse> getRegistration(@PathVariable Long id) {
        Registration registration = registrationService.findById(id);
        return ResponseEntity.ok(toResponse(registration));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RegistrationResponse> createRegistration(@Valid @RequestBody RegistrationRequest request) {
        Registration registration = registrationService.registerStudent(request.getCourseId(), request.getStudentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(registration));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long id) {
        registrationService.unregisterStudent(id);
        return ResponseEntity.noContent().build();
    }

    private RegistrationResponse toResponse(Registration registration) {
        String courseTitle = null;
        String studentName = null;
        try {
            Course course = courseService.getCourse(registration.getCourseId());
            courseTitle = course.getTitle();
        } catch (Exception e) {
            courseTitle = "Unknown Course";
        }
        try {
            User user = userService.getUser(registration.getStudentId());
            studentName = user.getName();
        } catch (Exception e) {
            studentName = "Unknown Student";
        }
        return RegistrationResponse.fromEntity(registration, courseTitle, studentName);
    }
}
