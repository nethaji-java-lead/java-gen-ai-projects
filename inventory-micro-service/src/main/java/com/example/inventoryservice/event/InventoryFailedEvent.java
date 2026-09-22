package com.example.inventoryservice.event;

public record InventoryFailedEvent(
        Long orderId,
        String orderNumber,
        String productId,
        Integer quantity,
        String reason
) {
}
