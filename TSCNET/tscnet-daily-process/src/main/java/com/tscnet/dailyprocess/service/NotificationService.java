package com.tscnet.dailyprocess.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public static final String TOPIC_PROCUREMENT_NOTIFICATIONS = "procurement-assessment-notifications";
    public static final String TOPIC_OPERATOR_ALERTS = "operator-alerts";
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public <K, V> void sendNotificationEvent(String topic, K key, V event) {
        kafkaTemplate.send(topic, (String) key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send Kafka notification event for key: {} to topic: {}", key, topic, ex);
                    } else {
                        log.info("Successfully published Kafka event to topic: {} [partition: {}, offset: {}, key: {}]",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                key);
                    }
                });
    }
}
