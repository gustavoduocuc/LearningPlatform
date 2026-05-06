package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Payment;
import com.duoc.LearningPlatform.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStudentId(Long studentId);

    boolean existsByStudentIdAndCourseIdAndPaymentStatus(Long studentId, Long courseId, PaymentStatus paymentStatus);
}
