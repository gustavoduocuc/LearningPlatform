package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.StudentEvaluation;
import com.duoc.LearningPlatform.model.StudentSubmission;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.CourseService;
import com.duoc.LearningPlatform.service.EvaluationService;
import com.duoc.LearningPlatform.service.SubmissionService;
import com.duoc.LearningPlatform.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationControllerTest {

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EvaluationController evaluationController;

    @Test
    void listsAllEvaluations() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation eval1 = new Evaluation(1L, "Midterm", 100, futureDate);
        Evaluation eval2 = new Evaluation(2L, "Final", 100, futureDate.plusDays(30));
        when(evaluationService.findAll()).thenReturn(List.of(eval1, eval2));

        ResponseEntity<List<com.duoc.LearningPlatform.dto.EvaluationResponse>> response = 
                evaluationController.listEvaluations(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void listsEvaluationsByCourseId() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation eval1 = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationService.findByCourseId(1L)).thenReturn(List.of(eval1));

        ResponseEntity<List<com.duoc.LearningPlatform.dto.EvaluationResponse>> response = 
                evaluationController.listEvaluations(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getsEvaluationById() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationService.findById(1L)).thenReturn(evaluation);

        ResponseEntity<com.duoc.LearningPlatform.dto.EvaluationResponse> response = 
                evaluationController.getEvaluation(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Midterm", response.getBody().getName());
    }

    @Test
    void createsEvaluationSuccessfully() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationService.createEvaluation(eq(1L), eq("Midterm"), eq(100), any(LocalDateTime.class)))
                .thenReturn(evaluation);

        com.duoc.LearningPlatform.dto.EvaluationRequest request = new com.duoc.LearningPlatform.dto.EvaluationRequest();
        request.setCourseId(1L);
        request.setName("Midterm");
        request.setMaximumScore(100);
        request.setApplicationDate(futureDate);

        ResponseEntity<com.duoc.LearningPlatform.dto.EvaluationResponse> response = 
                evaluationController.createEvaluation(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Midterm", response.getBody().getName());
    }

    @Test
    void updatesEvaluationSuccessfully() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        LocalDateTime newDate = LocalDateTime.now().plusDays(14);
        Evaluation evaluation = new Evaluation(1L, "Final Exam", 150, newDate);
        when(evaluationService.updateEvaluation(eq(1L), eq("Final Exam"), eq(150), any(LocalDateTime.class)))
                .thenReturn(evaluation);

        com.duoc.LearningPlatform.dto.EvaluationRequest request = new com.duoc.LearningPlatform.dto.EvaluationRequest();
        request.setCourseId(1L);
        request.setName("Final Exam");
        request.setMaximumScore(150);
        request.setApplicationDate(newDate);

        ResponseEntity<com.duoc.LearningPlatform.dto.EvaluationResponse> response = 
                evaluationController.updateEvaluation(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Final Exam", response.getBody().getName());
    }

    @Test
    void deletesEvaluationSuccessfully() {
        doNothing().when(evaluationService).deleteEvaluation(1L);

        ResponseEntity<Void> response = evaluationController.deleteEvaluation(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(evaluationService).deleteEvaluation(1L);
    }

    @Test
    void assignsGradeSuccessfully() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        when(evaluationService.assignGrade(2L, 1L, 85)).thenReturn(grade);

        com.duoc.LearningPlatform.dto.StudentEvaluationRequest request = new com.duoc.LearningPlatform.dto.StudentEvaluationRequest();
        request.setStudentId(1L);
        request.setScore(85);

        ResponseEntity<com.duoc.LearningPlatform.dto.StudentEvaluationResponse> response = 
                evaluationController.assignGrade(2L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(85, response.getBody().getScore());
    }

    @Test
    void listsGradesByEvaluationId() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(3L, 2L, 90);
        when(evaluationService.findGradesByEvaluationId(2L)).thenReturn(List.of(grade1, grade2));

        ResponseEntity<List<com.duoc.LearningPlatform.dto.StudentEvaluationResponse>> response =
                evaluationController.listGrades(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    // Submission controller tests
    @Test
    void studentSubmitsAssignmentSuccessfully() {
        Long evaluationId = 1L;
        String email = "ana@duoc.cl";
        String description = "My work";
        User student = new User("Ana", email, "password", Role.STUDENT);
        StudentSubmission submission = new StudentSubmission(evaluationId, 7L, description, "assignment.pdf", "application/pdf", new byte[]{1, 2, 3});

        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("assignment.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        try {
            when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(Optional.of(student));
        when(submissionService.submitAssignment(any(), eq(evaluationId), eq(description), eq("assignment.pdf"), eq("application/pdf"), any()))
                .thenReturn(submission);

        ResponseEntity<com.duoc.LearningPlatform.dto.SubmissionResponse> response =
                evaluationController.submitAssignment(evaluationId, description, file, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("assignment.pdf", response.getBody().getFileName());
    }

    @Test
    void professorListsSubmissionsSuccessfully() {
        // Arrange
        Long evaluationId = 1L;
        String email = "prof@duoc.cl";
        User professor = new User("Prof", email, "password", Role.PROFESSOR);

        StudentSubmission submission1 = new StudentSubmission(evaluationId, 7L, "Work 1", "file1.pdf", "application/pdf", new byte[]{1});
        StudentSubmission submission2 = new StudentSubmission(evaluationId, 8L, "Work 2", "file2.pdf", "application/pdf", new byte[]{2});

        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(Optional.of(professor));
        // Use any() for professorId since User doesn't have settable ID
        when(submissionService.findSubmissionsByEvaluationId(evaluationId, null))
                .thenReturn(List.of(submission1, submission2));

        // Act
        ResponseEntity<List<com.duoc.LearningPlatform.dto.SubmissionResponse>> response =
                evaluationController.listSubmissions(evaluationId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void studentListsTheirEvaluations() {
        // Arrange
        String email = "ana@duoc.cl";
        User student = new User("Ana", email, "password", Role.STUDENT);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

        Evaluation eval1 = new Evaluation(1L, "Midterm", 100, futureDate);
        Evaluation eval2 = new Evaluation(1L, "Final", 100, futureDate.plusDays(30));

        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(Optional.of(student));
        when(submissionService.findEvaluationsForStudent(any())).thenReturn(List.of(eval1, eval2));

        // Act
        ResponseEntity<List<com.duoc.LearningPlatform.dto.EvaluationResponse>> response =
                evaluationController.listMyEvaluations(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void studentViewsTheirOwnSubmission() {
        // Arrange
        Long evaluationId = 1L;
        String email = "ana@duoc.cl";
        User student = new User("Ana", email, "password", Role.STUDENT);
        StudentSubmission submission = new StudentSubmission(evaluationId, 7L, "My work", "assignment.pdf", "application/pdf", new byte[]{1, 2, 3});

        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(Optional.of(student));
        when(submissionService.findOwnSubmissionForEvaluation(any(), eq(evaluationId))).thenReturn(submission);

        // Act
        ResponseEntity<com.duoc.LearningPlatform.dto.SubmissionResponse> response =
                evaluationController.getMySubmission(evaluationId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("assignment.pdf", response.getBody().getFileName());
    }
}
