package com.example.paymentservice.consumer;

import com.example.paymentservice.enums.OrderStatus;
import com.example.paymentservice.event.OrderCreatedEvent;
import com.example.paymentservice.event.PaymentCompletedEvent;
import com.example.paymentservice.event.PaymentFailedEvent;
import com.example.paymentservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static com.example.paymentservice.constants.PaymentServiceConstants.*;

@Service
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderConsumer(
            ObjectMapper objectMapper,
            OrderRepository orderRepository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void consumeOrderCreatedEvent(String message) {

        try {

            System.out.println(
                    "Received order.created event: " + message
            );

            OrderCreatedEvent event =
                    objectMapper.readValue(
                            message,
                            OrderCreatedEvent.class
                    );

            Long orderId = event.orderId();

            // 1. Process payment
            boolean paymentSuccessful = processPayment(event);

            if (!paymentSuccessful) {
                // 5. Create payment.failed event
                PaymentFailedEvent paymentFailedEvent =
                        new PaymentFailedEvent(
                                event.orderId(),
                                "Payment failed"
                        );

                // 6. Publish payment.failed
                String failedPayload =
                        objectMapper.writeValueAsString(paymentFailedEvent);

                kafkaTemplate.send(
                        PAYMENT_FAILED_TOPIC,
                        String.valueOf(orderId),
                        failedPayload
                );

                return;
            }

            // 2. Update Order business status
            orderRepository.updateStatus(
                    orderId,
                    OrderStatus.PAYMENT_COMPLETED
            );

            // 3. Create payment.completed event
            PaymentCompletedEvent paymentEvent =
                    new PaymentCompletedEvent(
                            event.orderId(),
                            event.orderNumber(),
                            event.customerId(),
                            event.customerEmail(),
                            event.customerPhone(),
                            event.totalAmount(),
                            "PAYMENT_COMPLETED"
                    );

            // 4. Publish payment.completed
            String payload =
                    objectMapper.writeValueAsString(paymentEvent);

            kafkaTemplate.send(
                    PAYMENT_COMPLETED_EVENT,
                    String.valueOf(orderId),
                    payload
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to process order.created event: " + message
            );

            throw new RuntimeException(e);
        }
    }

    private boolean processPayment(OrderCreatedEvent event) {

        // Actual payment gateway integration here

        return true;
    }

    @KafkaListener(
            topics = INVENTORY_RESERVED_EVENT,
            groupId = "order-service"
    )
    public void consumeInventoryReserved(String message) {


        InventoryReservedEvent event = jsonMapper.readValue(message, InventoryReservedEvent.class);

        try {

            log.info(
                    "Inventory reserved for order {}. Product: {}, Quantity: {}",
                    event.orderNumber(),
                    event.productId(),
                    event.quantity()
            );

            Optional<Order> optionalOrder = orderRepository.findById(event.orderId());

            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setStatus(OrderStatus.INVENTORY_RESERVED);
                orderRepository.save(order);

            } else {
                throw new OrderException("Order not found for ID: " + event.orderId());
            }

        } catch (Exception e) {

            log.error(
                    "Failed to process inventoryreserved event: {}",
                    event.toString(),
                    e
            );

            throw new IllegalStateException(
                    "Unable to process inventoryreserved event",
                    e
            );
        }
    }
}