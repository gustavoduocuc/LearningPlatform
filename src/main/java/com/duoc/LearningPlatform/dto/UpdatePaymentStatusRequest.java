package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdatePaymentStatusRequest {

    @NotNull(message = "paymentStatus is required")
    private PaymentStatus paymentStatus;

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
