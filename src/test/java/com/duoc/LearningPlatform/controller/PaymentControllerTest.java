package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.PaymentRequest;
import com.duoc.LearningPlatform.dto.PaymentResponse;
import com.duoc.LearningPlatform.dto.UpdatePaymentStatusRequest;
import com.duoc.LearningPlatform.model.Payment;
import com.duoc.LearningPlatform.model.PaymentMethod;
import com.duoc.LearningPlatform.model.PaymentStatus;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.PaymentService;
import com.duoc.LearningPlatform.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void createsPaymentForAuthenticatedStudent() {
        User student = new User("Ana", "ana@duoc.cl", "password", Role.STUDENT);
        ReflectionTestUtils.setField(student, "id", 3L);
        when(authentication.getName()).thenReturn("ana@duoc.cl");
        when(userService.findByEmail("ana@duoc.cl")).thenReturn(Optional.of(student));

        Payment saved = new Payment(3L, 1L, new BigDecimal("99.00"), PaymentMethod.CREDIT_CARD,
                PaymentStatus.APPROVED, "SIM-abc");
        ReflectionTestUtils.setField(saved, "id", 10L);
        when(paymentService.createSimulatedPayment(eq(3L), eq(1L), eq(new BigDecimal("99.00")), eq(PaymentMethod.CREDIT_CARD)))
                .thenReturn(saved);

        PaymentRequest request = new PaymentRequest();
        request.setCourseId(1L);
        request.setAmount(new BigDecimal("99.00"));
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        ResponseEntity<PaymentResponse> response = paymentController.createPayment(authentication, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals(PaymentStatus.APPROVED, response.getBody().getPaymentStatus());
        verify(paymentService).createSimulatedPayment(3L, 1L, new BigDecimal("99.00"), PaymentMethod.CREDIT_CARD);
    }

    @Test
    void listsPaymentsForActor() {
        User admin = new User("Admin", "admin@duoc.cl", "password", Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(authentication.getName()).thenReturn("admin@duoc.cl");
        when(userService.findByEmail("admin@duoc.cl")).thenReturn(Optional.of(admin));

        Payment p = new Payment(2L, 5L, new BigDecimal("10"), PaymentMethod.BANK_TRANSFER, PaymentStatus.APPROVED, "r1");
        ReflectionTestUtils.setField(p, "id", 7L);
        when(paymentService.findPaymentsForActor(1L, Role.ADMIN)).thenReturn(List.of(p));

        ResponseEntity<List<PaymentResponse>> response = paymentController.listPayments(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(7L, response.getBody().get(0).getId());
    }

    @Test
    void getsPaymentById() {
        User student = new User("Ana", "ana@duoc.cl", "password", Role.STUDENT);
        ReflectionTestUtils.setField(student, "id", 3L);
        when(authentication.getName()).thenReturn("ana@duoc.cl");
        when(userService.findByEmail("ana@duoc.cl")).thenReturn(Optional.of(student));

        Payment p = new Payment(3L, 5L, new BigDecimal("10"), PaymentMethod.CREDIT_CARD, PaymentStatus.APPROVED, "r1");
        ReflectionTestUtils.setField(p, "id", 7L);
        when(paymentService.getPaymentForActor(7L, 3L, Role.STUDENT)).thenReturn(p);

        ResponseEntity<PaymentResponse> response = paymentController.getPayment(7L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7L, response.getBody().getId());
    }

    @Test
    void adminUpdatesPaymentStatus() {
        Payment p = new Payment(2L, 5L, new BigDecimal("10"), PaymentMethod.CREDIT_CARD, PaymentStatus.APPROVED, "r1");
        ReflectionTestUtils.setField(p, "id", 7L);
        p.applyStatus(PaymentStatus.CANCELLED);
        when(paymentService.updateStatus(7L, PaymentStatus.CANCELLED)).thenReturn(p);

        UpdatePaymentStatusRequest body = new UpdatePaymentStatusRequest();
        body.setPaymentStatus(PaymentStatus.CANCELLED);

        ResponseEntity<PaymentResponse> response = paymentController.updateStatus(7L, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService).updateStatus(7L, PaymentStatus.CANCELLED);
    }

    @Test
    void adminCancelsPayment() {
        Payment p = new Payment(2L, 5L, new BigDecimal("10"), PaymentMethod.CREDIT_CARD, PaymentStatus.CANCELLED, "r1");
        ReflectionTestUtils.setField(p, "id", 7L);
        when(paymentService.cancelPayment(7L)).thenReturn(p);

        ResponseEntity<PaymentResponse> response = paymentController.cancelPayment(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PaymentStatus.CANCELLED, response.getBody().getPaymentStatus());
    }
}
