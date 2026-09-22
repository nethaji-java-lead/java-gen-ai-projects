package com.example.noificationservice.consumer;

import com.example.noificationservice.event.PaymentCompletedEvent;
import com.example.noificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedConsumer {

    private final JsonMapper objectMapper;
    private final NotificationService notificationService;
    private static final String PAYMENT_COMPLETED_EVENT = "payment.completed";

    @KafkaListener(
            topics = PAYMENT_COMPLETED_EVENT,
            groupId = "notification-service"
    )
    public void consume(String message) {

        try {
            log.info("Received payment.completed event: {}", message);

            PaymentCompletedEvent event =
                    objectMapper.readValue(
                            message,
                            PaymentCompletedEvent.class
                    );

            notificationService.sendPaymentSuccessNotification(event);

        } catch (Exception e) {
            log.error(
                    "Failed to process payment.completed event",
                    e
            );

            throw new RuntimeException(e);
        }
    }
}