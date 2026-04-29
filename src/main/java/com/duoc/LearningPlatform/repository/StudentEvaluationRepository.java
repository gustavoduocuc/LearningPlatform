package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.StudentEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentEvaluationRepository extends JpaRepository<StudentEvaluation, Long> {

    List<StudentEvaluation> findByEvaluationId(Long evaluationId);

    List<StudentEvaluation> findByStudentId(Long studentId);

    Optional<StudentEvaluation> findByStudentIdAndEvaluationId(Long studentId, Long evaluationId);

    boolean existsByStudentIdAndEvaluationId(Long studentId, Long evaluationId);
}
