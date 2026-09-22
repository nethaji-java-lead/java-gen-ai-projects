package com.example.orderservice.event;

import java.math.BigDecimal;

public record InventoryReservedEvent(
        Long orderId,
        String orderNumber,
        String productId,
        Integer quantity,
        BigDecimal totalAmount
) {
}
