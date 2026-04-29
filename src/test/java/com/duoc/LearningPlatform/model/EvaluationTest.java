package com.duoc.LearningPlatform.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationTest {

    @Test
    void createsEvaluationWithValidData() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm Exam", 100, futureDate);

        assertNull(evaluation.getId());
        assertEquals(1L, evaluation.getCourseId());
        assertEquals("Midterm Exam", evaluation.getName());
        assertEquals(100, evaluation.getMaximumScore());
        assertEquals(futureDate, evaluation.getApplicationDate());
    }

    @Test
    void rejectsNullCourseId() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        assertThrows(IllegalArgumentException.class, () -> {
            new Evaluation(null, "Midterm", 100, futureDate);
        });
    }

    @Test
    void rejectsNullName() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        assertThrows(IllegalArgumentException.class, () -> {
            new Evaluation(1L, null, 100, futureDate);
        });
    }

    @Test
    void rejectsBlankName() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        assertThrows(IllegalArgumentException.class, () -> {
            new Evaluation(1L, "   ", 100, futureDate);
        });
    }

    @Test
    void rejectsZeroMaximumScore() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        assertThrows(IllegalArgumentException.class, () -> {
            new Evaluation(1L, "Midterm", 0, futureDate);
        });
    }

    @Test
    void rejectsNegativeMaximumScore() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        assertThrows(IllegalArgumentException.class, () -> {
            new Evaluation(1L, "Midterm", -10, futureDate);
        });
    }

    @Test
    void rejectsNullApplicationDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Evaluation(1L, "Midterm", 100, null);
        });
    }

    @Test
    void rejectsPastApplicationDate() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        assertThrows(IllegalArgumentException.class, () -> {
            new Evaluation(1L, "Midterm", 100, pastDate);
        });
    }

    @Test
    void allowsTodayAsApplicationDate() {
        LocalDateTime today = LocalDateTime.now();
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, today);
        assertEquals(today, evaluation.getApplicationDate());
    }

    @Test
    void updatesEvaluationDetails() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);

        LocalDateTime newDate = LocalDateTime.now().plusDays(14);
        evaluation.updateDetails("Final Exam", 150, newDate);

        assertEquals("Final Exam", evaluation.getName());
        assertEquals(150, evaluation.getMaximumScore());
        assertEquals(newDate, evaluation.getApplicationDate());
    }

    @Test
    void rejectsInvalidUpdate() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        Evaluation evaluation = new Evaluation(1L, "Midterm", 100, futureDate);

        assertThrows(IllegalArgumentException.class, () -> {
            evaluation.updateDetails("", 100, futureDate);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            evaluation.updateDetails("Valid", 0, futureDate);
        });
    }
}
