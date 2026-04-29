package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByCourseId(Long courseId);

    List<Registration> findByStudentId(Long studentId);

    Optional<Registration> findByCourseIdAndStudentId(Long courseId, Long studentId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
