package com.example.orderservice.kafkaconsumer;

import com.example.orderservice.event.PaymentCreatedEvent;
import com.example.orderservice.model.Order;
import com.example.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventConsumer {

    private static final String PAYMENT_CREATED_TOPIC =
            "payment-created";

    private final OrderService orderService;

    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = PAYMENT_CREATED_TOPIC,
            groupId = "order-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(PaymentCreatedEvent event) {

        log.info(
                "Received payment-created event: eventId={}, paymentId={}, amount={}",
                event.eventId(),
                event.paymentId(),
                event.billValue()
        );

        if (event.paymentId() == null) {
            throw new IllegalArgumentException("paymentId must not be null");
        }

        if (orderService.isOrderProcessed(event.paymentId())) {
            log.info(
                    "Skipping duplicate payment-created event. eventId={}, paymentId={}",
                    event.eventId(),
                    event.paymentId()
            );
            return;
        }

        // Simulate failure
        if ("NETHAJI-123".equals(event.name())) {

            throw new RuntimeException(
                    "Payment service unavailable"
            );
        }

        Order order = new Order();

        order.setPaymentId(event.paymentId());
        order.setCustomerName(event.name());
        order.setEmail(event.email());
        order.setAddress(event.address());
        order.setAmount(event.billValue());
        order.setStatus("PAYMENT_COMPLETED");

        orderService.processOrder(order);

        log.info(
                "Payment event processed successfully. eventId={}, paymentId={}",
                event.eventId(),
                event.paymentId()
        );
    }
}