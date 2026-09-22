package com.example.paymentservice.event;

import java.math.BigDecimal;

public record PaymentCreatedEvent(
        String eventId,
        Long paymentId,
        String name,
        String email,
        String address,
        BigDecimal billValue
) {
}