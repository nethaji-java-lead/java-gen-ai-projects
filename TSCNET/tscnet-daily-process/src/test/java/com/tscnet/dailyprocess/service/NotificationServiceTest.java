package com.tscnet.dailyprocess.service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendNotificationEvent_ShouldLogSuccess_WhenKafkaSendSucceeds() {
        String topic = NotificationService.TOPIC_PROCUREMENT_NOTIFICATIONS;
        String key = "123";
        String event = "test-event";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(topic, 0), 0L, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(null, recordMetadata);

        when(kafkaTemplate.send(topic, key, event)).thenReturn(future);

        notificationService.sendNotificationEvent(topic, key, event);

        // Complete the future successfully
        future.complete(sendResult);

        verify(kafkaTemplate, times(1)).send(topic, key, event);
    }

    @Test
    void sendNotificationEvent_ShouldLogError_WhenKafkaSendFails() {
        String topic = NotificationService.TOPIC_OPERATOR_ALERTS;
        String key = "456";
        String event = "error-event";

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();

        when(kafkaTemplate.send(topic, key, event)).thenReturn(future);

        notificationService.sendNotificationEvent(topic, key, event);

        // Complete the future exceptionally
        future.completeExceptionally(new RuntimeException("Kafka unreachable"));

        verify(kafkaTemplate, times(1)).send(topic, key, event);
    }
}