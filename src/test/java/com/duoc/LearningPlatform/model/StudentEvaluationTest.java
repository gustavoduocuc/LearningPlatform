package com.duoc.LearningPlatform.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StudentEvaluationTest {

    @Test
    void createsStudentEvaluationWithValidData() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);

        assertNull(grade.getId());
        assertEquals(1L, grade.getStudentId());
        assertEquals(2L, grade.getEvaluationId());
        assertEquals(85, grade.getScore());
        assertNull(grade.getEvaluatedAt());
    }

    @Test
    void createsStudentEvaluationWithNullScore() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, null);

        assertNull(grade.getScore());
    }

    @Test
    void rejectsNullStudentId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new StudentEvaluation(null, 2L, 85);
        });
    }

    @Test
    void rejectsNullEvaluationId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new StudentEvaluation(1L, null, 85);
        });
    }

    @Test
    void validatesStudentIdIsPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new StudentEvaluation(0L, 2L, 85);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new StudentEvaluation(-1L, 2L, 85);
        });
    }

    @Test
    void validatesEvaluationIdIsPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new StudentEvaluation(1L, 0L, 85);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new StudentEvaluation(1L, -1L, 85);
        });
    }

    @Test
    void rejectsNegativeScore() {
        assertThrows(IllegalArgumentException.class, () -> {
            new StudentEvaluation(1L, 2L, -5);
        });
    }

    @Test
    void acceptsZeroScore() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 0);
        assertEquals(0, grade.getScore());
    }

    @Test
    void setsEvaluatedAtOnAssignment() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        
        grade.onAssign();
        
        assertNotNull(grade.getEvaluatedAt());
        assertTrue(grade.getEvaluatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(grade.getEvaluatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void updatesScoreSuccessfully() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        grade.onAssign();
        LocalDateTime firstEvaluatedAt = grade.getEvaluatedAt();
        
        grade.updateScore(90);
        
        assertEquals(90, grade.getScore());
        assertEquals(firstEvaluatedAt, grade.getEvaluatedAt());
    }

    @Test
    void rejectsUpdatingWithNegativeScore() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        
        assertThrows(IllegalArgumentException.class, () -> {
            grade.updateScore(-10);
        });
    }

    @Test
    void rejectsUpdatingWithScoreExceedingMaximum() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        
        assertThrows(IllegalArgumentException.class, () -> {
            grade.updateScoreWithMaxCheck(120, 100);
        });
    }

    @Test
    void acceptsScoreEqualToMaximum() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        
        grade.updateScoreWithMaxCheck(100, 100);
        
        assertEquals(100, grade.getScore());
    }

    @Test
    void acceptsScoreBelowMaximum() {
        StudentEvaluation grade = new StudentEvaluation(1L, 2L, 85);
        
        grade.updateScoreWithMaxCheck(95, 100);
        
        assertEquals(95, grade.getScore());
    }

    @Test
    void twoGradesWithSameStudentAndEvaluationAreEqual() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(1L, 2L, 90);

        assertEquals(grade1, grade2);
        assertEquals(grade1.hashCode(), grade2.hashCode());
    }

    @Test
    void twoGradesWithDifferentStudentAreNotEqual() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(2L, 2L, 85);

        assertNotEquals(grade1, grade2);
    }

    @Test
    void twoGradesWithDifferentEvaluationAreNotEqual() {
        StudentEvaluation grade1 = new StudentEvaluation(1L, 2L, 85);
        StudentEvaluation grade2 = new StudentEvaluation(1L, 3L, 85);

        assertNotEquals(grade1, grade2);
    }
}
