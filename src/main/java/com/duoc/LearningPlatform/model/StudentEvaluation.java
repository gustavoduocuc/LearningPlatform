package com.duoc.LearningPlatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "student_evaluations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "evaluation_id"}, name = "uk_student_evaluation")
        })
public class StudentEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "evaluation_id", nullable = false)
    private Long evaluationId;

    @Column
    private Integer score;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    protected StudentEvaluation() {}

    public StudentEvaluation(Long studentId, Long evaluationId, Integer score) {
        validateStudentId(studentId);
        validateEvaluationId(evaluationId);
        validateScore(score);
        this.studentId = studentId;
        this.evaluationId = evaluationId;
        this.score = score;
    }

    @PrePersist
    public void onAssign() {
        if (score != null && evaluatedAt == null) {
            evaluatedAt = LocalDateTime.now();
        }
    }

    public void updateScore(Integer newScore) {
        validateScore(newScore);
        this.score = newScore;
    }

    public void updateScoreWithMaxCheck(Integer newScore, int maximumScore) {
        validateScore(newScore);
        if (newScore > maximumScore) {
            throw new IllegalArgumentException("Score cannot exceed maximum score of " + maximumScore);
        }
        this.score = newScore;
    }

    private void validateStudentId(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive");
        }
    }

    private void validateEvaluationId(Long evaluationId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("Evaluation ID cannot be null");
        }
        if (evaluationId <= 0) {
            throw new IllegalArgumentException("Evaluation ID must be positive");
        }
    }

    private void validateScore(Integer score) {
        if (score != null && score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public Integer getScore() {
        return score;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentEvaluation that = (StudentEvaluation) o;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(evaluationId, that.evaluationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, evaluationId);
    }
}
