package com.example.inventoryservice.service;

import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.entity.Product;
import com.example.inventoryservice.event.InventoryFailedEvent;
import com.example.inventoryservice.event.InventoryReservedEvent;
import com.example.inventoryservice.event.OrderCreatedEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@Slf4j
@Service
public class InventoryService {

    private static final String ORDER_CREATED_EVENT = "order.created";

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryEventProducer inventoryEventProducer;

    @Autowired
    private ObjectMapper objectMapper;

    public InventoryService(ProductRepository productRepository, InventoryRepository inventoryRepository, InventoryEventProducer inventoryEventProducer) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryEventProducer = inventoryEventProducer;
    }

    @Transactional
    public void reserveInventory(
            String productId,
            int quantity
    ) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Product product = productRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: " + productId
                        )
                );

        Inventory inventory = inventoryRepository
                .findByProductProductId(product.getProductId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for product: "
                                        + productId
                        )
                );

        if (inventory.getAvailableQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient inventory for product: "
                            + productId
            );
        }

        try {

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() - quantity
            );

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() + quantity
            );


            inventoryRepository.save(inventory);

        } catch (ObjectOptimisticLockingFailureException |
                 OptimisticLockException e) {

            throw new IllegalStateException(
                    "Inventory was modified by another transaction. "
                            + "Please retry.",
                    e
            );
        }
    }


    @Transactional
    public void releaseInventory(
            String productId,
            int quantity
    ) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Inventory inventory = inventoryRepository
                .findByProductProductId(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for product: "
                                        + productId
                        )
                );

        if (inventory.getReservedQuantity() < quantity) {
            throw new IllegalStateException(
                    "Cannot release more inventory than reserved"
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + quantity
        );

        inventoryRepository.save(inventory);
    }


    @KafkaListener(
            topics = ORDER_CREATED_EVENT,
            groupId = "inventory-service"
    )
    public void consumeOrderCreated(String message) {

        OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);

        try {

            reserveInventory(
                    event.productId(),
                    event.quantity()
            );

           Product product =  productRepository.findByProductId(event.productId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Product not found: " + event.productId()
                            )
                    );

            InventoryReservedEvent reservedEvent =
                    new InventoryReservedEvent(
                            event.orderId(),
                            event.orderNumber(),
                            event.productId(),
                            event.quantity(),
                            product.getPrice().multiply(BigDecimal.valueOf(event.quantity())));

            inventoryEventProducer.publishInventoryReserved(reservedEvent);

        } catch (Exception e) {

            InventoryFailedEvent failedEvent =
                    new InventoryFailedEvent(
                            event.orderId(),
                            event.orderNumber(),
                            event.productId(),
                            event.quantity(),
                            e.getMessage()
                    );
            log.error(
                    "Failed to reserve inventory for order {}. Reason: {}",
                    event.orderNumber(),
                    e.getMessage()
            );

            inventoryEventProducer.publishInventoryFailed(failedEvent);
        }
    }
}
