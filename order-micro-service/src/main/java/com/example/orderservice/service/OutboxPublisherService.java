package com.example.orderservice.service;

import com.example.orderservice.enums.EventStatus;
import com.example.orderservice.exception.OrderEventNotFoundException;
import com.example.orderservice.model.OrderEvent;
import com.example.orderservice.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {

    private final OrderEventRepository orderEventRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    //@Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {

        log.info("Finding Pending Events to process");

        List<OrderEvent> events =
                orderEventRepository.findByStatus(EventStatus.PENDING);

        for (OrderEvent event : events) {

            try {

                log.info(
                        "Publishing outbox event. eventId={}, eventType={}, topic={}",
                        event.getId(),
                        event.getEventType(),
                        event.getTopic()
                );

                kafkaTemplate
                        .send(
                                event.getTopic(),
                                String.valueOf(event.getOrderId()),
                                event.getPayload()
                        )
                        .whenComplete((result, exception) -> {

                            if (exception == null) {
                                markAsPublished(event.getId());
                            } else {
                                markAsFailed(event.getId(), exception);
                            }
                        });

            } catch (Exception ex) {

                log.error(
                        "Failed to publish outbox event. eventId={}",
                        event.getId(),
                        ex
                );
            }
        }
    }

    private void markAsPublished(Long eventId) {

        orderEventRepository.findById(eventId)
                .ifPresent(event -> {
                    event.setStatus(EventStatus.PUBLISHED);
                    event.setPublishedAt(LocalDateTime.now());
                    orderEventRepository.save(event);
                });
    }

    private void markAsFailed(Long eventId, Throwable exception) {

        orderEventRepository.findById(eventId)
                .ifPresent(event -> {
                    event.setStatus(EventStatus.FAILED);
                    event.setRetryCount(event.getRetryCount() + 1);
                    orderEventRepository.save(event);
                });

        log.error(
                "Kafka publishing failed. eventId={}",
                eventId,
                exception
        );
    }

    public Optional<OrderEvent> getOrderEventByOrderId(Long orderId) throws OrderEventNotFoundException {
        return Optional.of(orderEventRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderEventNotFoundException("Order event not found for orderId: " + orderId)));
    }
}