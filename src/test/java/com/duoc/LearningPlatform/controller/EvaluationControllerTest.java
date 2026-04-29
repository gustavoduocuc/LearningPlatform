package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.StudentEvaluation;
import com.duoc.LearningPlatform.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationControllerTest {

    @Mock
    private EvaluationService evaluationService;

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
}
