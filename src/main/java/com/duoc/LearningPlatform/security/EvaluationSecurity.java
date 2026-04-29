package com.duoc.LearningPlatform.security;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.CourseService;
import com.duoc.LearningPlatform.service.EvaluationService;
import com.duoc.LearningPlatform.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("evaluationSecurity")
public class EvaluationSecurity {

    private final UserService userService;
    private final EvaluationService evaluationService;
    private final CourseService courseService;

    public EvaluationSecurity(UserService userService,
                               EvaluationService evaluationService,
                               CourseService courseService) {
        this.userService = userService;
        this.evaluationService = evaluationService;
        this.courseService = courseService;
    }

    public boolean canManageEvaluation(Long evaluationId, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }

        // Admin can manage all
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Professor can manage evaluations for their courses
        if (user.getRole() == Role.PROFESSOR) {
            try {
                Evaluation evaluation = evaluationService.findById(evaluationId);
                Course course = courseService.getCourse(evaluation.getCourseId());
                return user.getId().equals(course.getProfessorId());
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public boolean canViewEvaluation(Long evaluationId, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }

        // Admin and Professor can view all evaluations
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROFESSOR) {
            return true;
        }

        // Students can view evaluations for courses they are registered in
        // This is handled at the service/controller level with filtering
        return true;
    }
}
