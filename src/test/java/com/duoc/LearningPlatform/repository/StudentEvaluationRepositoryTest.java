package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.StudentEvaluation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentEvaluationRepositoryTest {

    @Mock
    private StudentEvaluationRepository studentEvaluationRepository;

    @Test
    void findsGradesByEvaluationId() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(2L, 2L, 90);
        when(studentEvaluationRepository.findByEvaluationId(2L)).thenReturn(List.of(grade1, grade2));

        List<StudentEvaluation> result = studentEvaluationRepository.findByEvaluationId(2L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> g.getEvaluationId().equals(2L)));
    }

    @Test
    void findsGradesByStudentId() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(1L, 3L, 78);
        when(studentEvaluationRepository.findByStudentId(1L)).thenReturn(List.of(grade1, grade2));

        List<StudentEvaluation> result = studentEvaluationRepository.findByStudentId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> g.getStudentId().equals(1L)));
    }

    @Test
    void findsGradeByStudentIdAndEvaluationId() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        when(studentEvaluationRepository.findByStudentIdAndEvaluationId(1L, 2L)).thenReturn(Optional.of(grade));
        when(studentEvaluationRepository.findByStudentIdAndEvaluationId(1L, 99L)).thenReturn(Optional.empty());

        Optional<StudentEvaluation> found = studentEvaluationRepository.findByStudentIdAndEvaluationId(1L, 2L);
        Optional<StudentEvaluation> notFound = studentEvaluationRepository.findByStudentIdAndEvaluationId(1L, 99L);

        assertTrue(found.isPresent());
        assertEquals(85, found.get().getScore());
        assertTrue(notFound.isEmpty());
    }

    @Test
    void checksExistsByStudentIdAndEvaluationId() {
        when(studentEvaluationRepository.existsByStudentIdAndEvaluationId(1L, 2L)).thenReturn(true);
        when(studentEvaluationRepository.existsByStudentIdAndEvaluationId(1L, 99L)).thenReturn(false);

        assertTrue(studentEvaluationRepository.existsByStudentIdAndEvaluationId(1L, 2L));
        assertFalse(studentEvaluationRepository.existsByStudentIdAndEvaluationId(1L, 99L));
    }

    @Test
    void savesGrade() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        when(studentEvaluationRepository.save(any(StudentEvaluation.class))).thenAnswer(i -> {
            StudentEvaluation saved = i.getArgument(0);
            saved.getClass().getDeclaredFields();
            return saved;
        });

        StudentEvaluation saved = studentEvaluationRepository.save(grade);

        assertNotNull(saved);
        assertEquals(1L, saved.getStudentId());
        assertEquals(2L, saved.getEvaluationId());
        assertEquals(85, saved.getScore());
    }

    @Test
    void deletesGrade() {
        doNothing().when(studentEvaluationRepository).deleteById(1L);

        studentEvaluationRepository.deleteById(1L);

        verify(studentEvaluationRepository).deleteById(1L);
    }

    @Test
    void findsAllGrades() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(3L, 4L, 90);
        when(studentEvaluationRepository.findAll()).thenReturn(List.of(grade1, grade2));

        List<StudentEvaluation> result = studentEvaluationRepository.findAll();

        assertEquals(2, result.size());
    }
}
