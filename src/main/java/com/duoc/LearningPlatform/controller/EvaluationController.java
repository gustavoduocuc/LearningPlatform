package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.EvaluationRequest;
import com.duoc.LearningPlatform.dto.EvaluationResponse;
import com.duoc.LearningPlatform.dto.StudentEvaluationRequest;
import com.duoc.LearningPlatform.dto.StudentEvaluationResponse;
import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.StudentEvaluation;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.CourseService;
import com.duoc.LearningPlatform.service.EvaluationService;
import com.duoc.LearningPlatform.service.UserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final CourseService courseService;
    private final UserService userService;

    public EvaluationController(EvaluationService evaluationService,
                                   CourseService courseService,
                                   UserService userService) {
        this.evaluationService = evaluationService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<EvaluationResponse>> listEvaluations(
            @RequestParam(required = false) Long courseId) {
        List<Evaluation> evaluations;
        if (courseId != null) {
            evaluations = evaluationService.findByCourseId(courseId);
        } else {
            evaluations = evaluationService.findAll();
        }

        List<EvaluationResponse> response = evaluations.stream()
                .map(this::toEvaluationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<EvaluationResponse> getEvaluation(@PathVariable Long id) {
        Evaluation evaluation = evaluationService.findById(id);
        return ResponseEntity.ok(toEvaluationResponse(evaluation));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<EvaluationResponse> createEvaluation(@Valid @RequestBody EvaluationRequest request) {
        Evaluation evaluation = evaluationService.createEvaluation(
                request.getCourseId(),
                request.getName(),
                request.getMaximumScore(),
                request.getApplicationDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toEvaluationResponse(evaluation));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<EvaluationResponse> updateEvaluation(@PathVariable Long id, @Valid @RequestBody EvaluationRequest request) {
        Evaluation evaluation = evaluationService.updateEvaluation(
                id,
                request.getName(),
                request.getMaximumScore(),
                request.getApplicationDate()
        );
        return ResponseEntity.ok(toEvaluationResponse(evaluation));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        evaluationService.deleteEvaluation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<StudentEvaluationResponse> assignGrade(@PathVariable Long id, @Valid @RequestBody StudentEvaluationRequest request) {
        StudentEvaluation grade = evaluationService.assignGrade(id, request.getStudentId(), request.getScore());
        return ResponseEntity.ok(toStudentEvaluationResponse(grade));
    }

    @GetMapping("/{id}/grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<StudentEvaluationResponse>> listGrades(@PathVariable Long id) {
        List<StudentEvaluation> grades = evaluationService.findGradesByEvaluationId(id);
        List<StudentEvaluationResponse> response = grades.stream()
                .map(this::toStudentEvaluationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private EvaluationResponse toEvaluationResponse(Evaluation evaluation) {
        String courseTitle = null;
        try {
            Course course = courseService.getCourse(evaluation.getCourseId());
            courseTitle = course.getTitle();
        } catch (Exception e) {
            courseTitle = "Unknown Course";
        }
        return EvaluationResponse.fromEntity(evaluation, courseTitle);
    }

    private StudentEvaluationResponse toStudentEvaluationResponse(StudentEvaluation grade) {
        String studentName = null;
        String evaluationName = null;
        try {
            User user = userService.getUser(grade.getStudentId());
            studentName = user.getName();
        } catch (Exception e) {
            studentName = "Unknown Student";
        }
        try {
            Evaluation evaluation = evaluationService.findById(grade.getEvaluationId());
            evaluationName = evaluation.getName();
        } catch (Exception e) {
            evaluationName = "Unknown Evaluation";
        }
        return StudentEvaluationResponse.fromEntity(grade, studentName, evaluationName);
    }
}
