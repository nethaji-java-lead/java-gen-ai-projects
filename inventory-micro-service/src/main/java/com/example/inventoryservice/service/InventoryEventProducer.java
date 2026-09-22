package com.example.inventoryservice.service;

import com.example.inventoryservice.event.InventoryFailedEvent;
import com.example.inventoryservice.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private static final String INVENTORY_RESERVED_EVENT =
            "inventory.reserved";

    private static final String INVENTORY_FAILED_EVENT =
            "inventory.failed";

    private final JsonMapper jsonMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInventoryReserved(InventoryReservedEvent event) {
        String message = jsonMapper.writeValueAsString(event);
        kafkaTemplate.send(
                INVENTORY_RESERVED_EVENT,
                event.orderNumber(),
                message
        );
    }

    public void publishInventoryFailed(InventoryFailedEvent event) {

        String message = jsonMapper.writeValueAsString(event);
        kafkaTemplate.send(
                INVENTORY_FAILED_EVENT,
                event.orderNumber(),
                message
        );
    }
}
