package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Payment;
import com.duoc.LearningPlatform.model.PaymentMethod;
import com.duoc.LearningPlatform.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRepositoryTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    void findsPaymentsByStudentId() {
        Payment p1 = new Payment(2L, 1L, new BigDecimal("10"), PaymentMethod.CREDIT_CARD, PaymentStatus.APPROVED, "a");
        Payment p2 = new Payment(2L, 3L, new BigDecimal("20"), PaymentMethod.BANK_TRANSFER, PaymentStatus.APPROVED, "b");
        when(paymentRepository.findByStudentId(2L)).thenReturn(List.of(p1, p2));

        List<Payment> result = paymentRepository.findByStudentId(2L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getStudentId().equals(2L)));
    }

    @Test
    void existsByStudentIdAndCourseIdAndApprovedStatus() {
        when(paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(2L, 1L, PaymentStatus.APPROVED))
                .thenReturn(true);
        when(paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(2L, 1L, PaymentStatus.PENDING))
                .thenReturn(false);

        assertTrue(paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(2L, 1L, PaymentStatus.APPROVED));
        assertFalse(paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(2L, 1L, PaymentStatus.PENDING));
    }

    @Test
    void savesPayment() {
        Payment payment = new Payment(2L, 1L, new BigDecimal("15.50"), PaymentMethod.CREDIT_CARD, PaymentStatus.APPROVED, "ref");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100L);
            return saved;
        });

        Payment saved = paymentRepository.save(payment);

        assertEquals(100L, saved.getId());
        assertEquals(new BigDecimal("15.50"), saved.getAmount());
    }
}
