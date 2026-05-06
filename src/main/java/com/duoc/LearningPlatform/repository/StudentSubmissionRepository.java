package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.StudentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSubmissionRepository extends JpaRepository<StudentSubmission, Long> {

    List<StudentSubmission> findByEvaluationId(Long evaluationId);

    Optional<StudentSubmission> findByStudentIdAndEvaluationId(Long studentId, Long evaluationId);

    boolean existsByStudentIdAndEvaluationId(Long studentId, Long evaluationId);
}
