package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.PaymentRequest;
import com.duoc.LearningPlatform.dto.PaymentResponse;
import com.duoc.LearningPlatform.dto.UpdatePaymentStatusRequest;
import com.duoc.LearningPlatform.model.Payment;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.PaymentService;
import com.duoc.LearningPlatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PaymentResponse> createPayment(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {
        User student = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        Payment payment = paymentService.createSimulatedPayment(
                student.getId(),
                request.getCourseId(),
                request.getAmount(),
                request.getPaymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.fromEntity(payment));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<List<PaymentResponse>> listPayments(Authentication authentication) {
        User actor = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        List<Payment> payments = paymentService.findPaymentsForActor(actor.getId(), actor.getRole());
        List<PaymentResponse> body = payments.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id, Authentication authentication) {
        User actor = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        Payment payment = paymentService.getPaymentForActor(id, actor.getId(), actor.getRole());
        return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        Payment payment = paymentService.updateStatus(id, request.getPaymentStatus());
        return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long id) {
        Payment payment = paymentService.cancelPayment(id);
        return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
    }
}
