package com.example.inventoryservice.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        Long customerId,
        String customerEmail,
        String customerPhone,
        boolean emailNotifications,
        boolean smsNotifications,
        BigDecimal totalAmount,
        String productId,
        String productCode,
        Integer quantity
) {
}