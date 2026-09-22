package com.example.orderservice.event;

public record InventoryFailedEvent(
        Long orderId,
        String orderNumber,
        String productId,
        Integer quantity,
        String reason) {
}
