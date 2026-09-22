package com.example.paymentservice.producer;

import com.example.paymentservice.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private static final String PAYMENT_CREATED_TOPIC = "payment-created";

    private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    public void publishPaymentCreatedEvent(PaymentCreatedEvent event) {

        kafkaTemplate.send(
                PAYMENT_CREATED_TOPIC,
                String.valueOf(event.paymentId()),
                event
        ).whenComplete((result, ex) -> {

            if (ex != null) {

                log.error(
                        "Failed to publish payment event. paymentId={}",
                        event.paymentId(),
                        ex
                );

            } else {

                log.info(
                        "Payment event published successfully. paymentId={}, topic={}, partition={}, offset={}",
                        event.paymentId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            }
        });
    }
}