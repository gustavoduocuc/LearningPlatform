package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    Optional<PasswordResetCode> findByEmail(String email);

    void deleteByEmail(String email);
}
