package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Evaluation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationRepositoryTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Test
    void findsEvaluationsByCourseId() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation eval1 = new Evaluation(1L, "Midterm", 100, futureDate);
        Evaluation eval2 = new Evaluation(1L, "Final", 100, futureDate.plusDays(30));
        when(evaluationRepository.findByCourseId(1L)).thenReturn(List.of(eval1, eval2));

        List<Evaluation> result = evaluationRepository.findByCourseId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getCourseId().equals(1L)));
    }

    @Test
    void findsEvaluationById() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));

        Optional<Evaluation> found = evaluationRepository.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Midterm", found.get().getName());
    }

    @Test
    void findsAllEvaluations() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation eval1 = new Evaluation(1L, "Midterm", 100, futureDate);
        Evaluation eval2 = new Evaluation(2L, "Quiz", 50, futureDate.plusDays(1));
        when(evaluationRepository.findAll()).thenReturn(List.of(eval1, eval2));

        List<Evaluation> result = evaluationRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void savesEvaluation() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(i -> {
            Evaluation saved = i.getArgument(0);
            saved.getClass().getDeclaredFields();
            return saved;
        });

        Evaluation saved = evaluationRepository.save(evaluation);

        assertNotNull(saved);
        assertEquals("Midterm", saved.getName());
        assertEquals(100, saved.getMaximumScore());
    }

    @Test
    void deletesEvaluation() {
        doNothing().when(evaluationRepository).deleteById(1L);

        evaluationRepository.deleteById(1L);

        verify(evaluationRepository).deleteById(1L);
    }

    @Test
    void checksExistsByCourseIdAndName() {
        when(evaluationRepository.existsByCourseIdAndName(1L, "Midterm")).thenReturn(true);
        when(evaluationRepository.existsByCourseIdAndName(1L, "Nonexistent")).thenReturn(false);

        assertTrue(evaluationRepository.existsByCourseIdAndName(1L, "Midterm"));
        assertFalse(evaluationRepository.existsByCourseIdAndName(1L, "Nonexistent"));
    }
}
