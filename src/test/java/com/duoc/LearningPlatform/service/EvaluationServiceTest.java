package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.StudentEvaluation;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.CourseRepository;
import com.duoc.LearningPlatform.repository.EvaluationRepository;
import com.duoc.LearningPlatform.repository.StudentEvaluationRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private StudentEvaluationRepository studentEvaluationRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    void createsEvaluationSuccessfully() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Course course = new Course("Java 101", "Description", 1L);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(evaluation);

        Evaluation result = evaluationService.createEvaluation(1L, "Midterm", 100, futureDate);

        assertNotNull(result);
        assertEquals("Midterm", result.getName());
        assertEquals(100, result.getMaximumScore());
        verify(evaluationRepository).save(any(Evaluation.class));
    }

    @Test
    void throwsWhenCreatingEvaluationForNonexistentCourse() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.createEvaluation(99L, "Midterm", 100, futureDate);
        });

        assertEquals("Course not found with id: 99", exception.getMessage());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void updatesEvaluationSuccessfully() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        LocalDateTime newDate = LocalDateTime.now().plusDays(14);
        Evaluation existingEvaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        Evaluation updatedEvaluation = new Evaluation(1L, "Final Exam", 150, newDate);

        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(existingEvaluation));
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(updatedEvaluation);

        Evaluation result = evaluationService.updateEvaluation(1L, "Final Exam", 150, newDate);

        assertNotNull(result);
        assertEquals("Final Exam", result.getName());
        assertEquals(150, result.getMaximumScore());
    }

    @Test
    void throwsWhenUpdatingNonexistentEvaluation() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.updateEvaluation(99L, "Midterm", 100, futureDate);
        });

        assertEquals("Evaluation not found with id: 99", exception.getMessage());
    }

    @Test
    void findsEvaluationsByCourseId() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation eval1 = new Evaluation(1L, "Midterm", 100, futureDate);
        Evaluation eval2 = new Evaluation(1L, "Final", 100, futureDate.plusDays(30));
        when(evaluationRepository.findByCourseId(1L)).thenReturn(List.of(eval1, eval2));

        List<Evaluation> result = evaluationService.findByCourseId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void findsAllEvaluations() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation eval1 = new Evaluation(1L, "Midterm", 100, futureDate);
        Evaluation eval2 = new Evaluation(2L, "Quiz", 50, futureDate.plusDays(1));
        when(evaluationRepository.findAll()).thenReturn(List.of(eval1, eval2));

        List<Evaluation> result = evaluationService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void assignsGradeSuccessfully() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        User student = new User("John", "john@example.com", "password", Role.STUDENT);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        StudentEvaluation grade = new StudentEvaluation(2L, 1L, 85);

        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(studentEvaluationRepository.existsByStudentIdAndEvaluationId(2L, 1L)).thenReturn(false);
        when(studentEvaluationRepository.save(any(StudentEvaluation.class))).thenReturn(grade);

        StudentEvaluation result = evaluationService.assignGrade(1L, 2L, 85);

        assertNotNull(result);
        assertEquals(85, result.getScore());
        assertEquals(2L, result.getStudentId());
        assertEquals(1L, result.getEvaluationId());
    }

    @Test
    void throwsWhenAssigningGradeForNonexistentEvaluation() {
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.assignGrade(99L, 2L, 85);
        });

        assertEquals("Evaluation not found with id: 99", exception.getMessage());
    }

    @Test
    void throwsWhenAssigningGradeForNonexistentStudent() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.assignGrade(1L, 99L, 85);
        });

        assertEquals("Student not found with id: 99", exception.getMessage());
    }

    @Test
    void throwsWhenAssigningGradeToNonStudent() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        User professor = new User("Prof", "prof@example.com", "password", Role.PROFESSOR);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.assignGrade(1L, 2L, 85);
        });

        assertEquals("User with id 2 is not a student", exception.getMessage());
    }

    @Test
    void throwsWhenAssigningGradeExceedingMaximum() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        User student = new User("John", "john@example.com", "password", Role.STUDENT);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.assignGrade(1L, 2L, 120);
        });

        assertEquals("Score cannot exceed maximum score of 100", exception.getMessage());
    }

    @Test
    void updatesGradeSuccessfully() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        StudentEvaluation existingGrade = new StudentEvaluation(2L, 1L, 75);

        when(studentEvaluationRepository.findById(1L)).thenReturn(Optional.of(existingGrade));
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));
        when(studentEvaluationRepository.save(any(StudentEvaluation.class))).thenAnswer(i -> i.getArgument(0));

        StudentEvaluation result = evaluationService.updateGrade(1L, 90);

        assertEquals(90, result.getScore());
    }

    @Test
    void findsGradesByEvaluationId() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(3L, 2L, 90);
        when(studentEvaluationRepository.findByEvaluationId(2L)).thenReturn(List.of(grade1, grade2));

        List<StudentEvaluation> result = evaluationService.findGradesByEvaluationId(2L);

        assertEquals(2, result.size());
    }

    @Test
    void findsGradesByStudentId() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(1L, 3L, 78);
        when(studentEvaluationRepository.findByStudentId(1L)).thenReturn(List.of(grade1, grade2));

        List<StudentEvaluation> result = evaluationService.findGradesByStudentId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void findsEvaluationById() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));

        Evaluation result = evaluationService.findById(1L);

        assertNotNull(result);
        assertEquals("Midterm", result.getName());
    }

    @Test
    void throwsWhenFindingNonexistentEvaluation() {
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.findById(99L);
        });

        assertEquals("Evaluation not found with id: 99", exception.getMessage());
    }

    @Test
    void deletesEvaluation() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));
        doNothing().when(evaluationRepository).deleteById(1L);

        evaluationService.deleteEvaluation(1L);

        verify(evaluationRepository).deleteById(1L);
    }
}
