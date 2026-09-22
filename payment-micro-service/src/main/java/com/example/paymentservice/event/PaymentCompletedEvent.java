package com.example.paymentservice.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        Long orderId,
        String orderNumber,
        Long customerId,
        String customerEmail,
        String customerPhone,
        BigDecimal amount,
        String status
) {}