package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.exception.ResourceNotFoundException;
import com.duoc.LearningPlatform.model.Payment;
import com.duoc.LearningPlatform.model.PaymentMethod;
import com.duoc.LearningPlatform.model.PaymentStatus;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.PaymentRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final RegistrationService registrationService;

    public PaymentService(PaymentRepository paymentRepository,
                          UserRepository userRepository,
                          CourseService courseService,
                          RegistrationService registrationService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.registrationService = registrationService;
    }

    @Transactional
    public Payment createSimulatedPayment(Long studentId, Long courseId, BigDecimal amount, PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("User with id " + studentId + " is not a student");
        }

        courseService.getCourse(courseId);

        if (!registrationService.isRegistered(courseId, studentId)) {
            throw new IllegalStateException("Student must be registered for the course before paying");
        }

        if (paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(studentId, courseId, PaymentStatus.APPROVED)) {
            throw new IllegalStateException("An approved payment already exists for this student and course");
        }

        String reference = "SIM-" + UUID.randomUUID();
        Payment payment = new Payment(studentId, courseId, amount, paymentMethod, PaymentStatus.APPROVED, reference);
        return paymentRepository.save(payment);
    }

    public List<Payment> findPaymentsForActor(Long userId, Role role) {
        if (role == Role.ADMIN) {
            return paymentRepository.findAll();
        }
        if (role == Role.STUDENT) {
            return paymentRepository.findByStudentId(userId);
        }
        return List.of();
    }

    public Payment getPaymentForActor(Long paymentId, Long actorUserId, Role actorRole) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (actorRole == Role.ADMIN) {
            return payment;
        }
        if (actorRole == Role.STUDENT && payment.getStudentId().equals(actorUserId)) {
            return payment;
        }
        throw new AccessDeniedException("Cannot access this payment");
    }

    @Transactional
    public Payment updateStatus(Long paymentId, PaymentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Payment status cannot be null");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        assertTransitionAllowed(payment.getPaymentStatus(), newStatus);
        payment.applyStatus(newStatus);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        PaymentStatus current = payment.getPaymentStatus();
        if (current == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Payment is already cancelled");
        }
        if (current == PaymentStatus.REJECTED) {
            throw new IllegalStateException("Cannot cancel a rejected payment");
        }
        if (current == PaymentStatus.PENDING || current == PaymentStatus.APPROVED) {
            payment.applyStatus(PaymentStatus.CANCELLED);
            return paymentRepository.save(payment);
        }
        throw new IllegalStateException("Cannot cancel payment in status: " + current);
    }

    private void assertTransitionAllowed(PaymentStatus from, PaymentStatus to) {
        if (from == to) {
            return;
        }
        if (from == PaymentStatus.PENDING) {
            if (to == PaymentStatus.APPROVED || to == PaymentStatus.REJECTED || to == PaymentStatus.CANCELLED) {
                return;
            }
        }
        if (from == PaymentStatus.APPROVED && to == PaymentStatus.CANCELLED) {
            return;
        }
        throw new IllegalStateException("Invalid payment status transition from " + from + " to " + to);
    }
}
