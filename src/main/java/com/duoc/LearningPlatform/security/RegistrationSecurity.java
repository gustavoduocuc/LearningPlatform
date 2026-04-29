package com.duoc.LearningPlatform.security;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Registration;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.CourseService;
import com.duoc.LearningPlatform.service.RegistrationService;
import com.duoc.LearningPlatform.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("registrationSecurity")
public class RegistrationSecurity {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final CourseService courseService;

    public RegistrationSecurity(UserService userService,
                                 RegistrationService registrationService,
                                 CourseService courseService) {
        this.userService = userService;
        this.registrationService = registrationService;
        this.courseService = courseService;
    }

    public boolean isOwner(Long studentId, Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .map(user -> user.getId().equals(studentId) && user.getRole() == Role.STUDENT)
                .orElse(false);
    }

    public boolean isCurrentStudent(Long studentId, Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .map(user -> user.getId().equals(studentId) && user.getRole() == Role.STUDENT)
                .orElse(false);
    }

    public boolean canViewRegistration(Long registrationId, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }

        // Admin can view all
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        Registration registration = registrationService.findById(registrationId);

        // Student can view their own
        if (user.getRole() == Role.STUDENT && user.getId().equals(registration.getStudentId())) {
            return true;
        }

        // Professor can view registrations for their courses
        if (user.getRole() == Role.PROFESSOR) {
            try {
                Course course = courseService.getCourse(registration.getCourseId());
                return user.getId().equals(course.getProfessorId());
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }
}
