package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByCourseId(Long courseId);

    boolean existsByCourseIdAndName(Long courseId, String name);
}
