package com.example.paymentservice.event;

public record PaymentFailedEvent(
        Long orderId,
        String reason
) {}