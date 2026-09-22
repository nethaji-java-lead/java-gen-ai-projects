package com.example.orderservice.event;

public record PaymentFailedEvent(
        Long orderId,
        String reason
) {}