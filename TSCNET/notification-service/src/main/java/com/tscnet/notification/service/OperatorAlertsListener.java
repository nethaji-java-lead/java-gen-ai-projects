package com.tscnet.notification.service;

import com.tscnet.notification.event.OperatorAlertEvent;
import com.tscnet.notification.event.ProcessExecutionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(topics = OperatorAlertsListener.TOPIC_OPERATOR_ALERTS, groupId = OperatorAlertsListener.GROUP_ID)
public class OperatorAlertsListener {

    public static final String GROUP_ID = "notification-service-group";
    public static final String TOPIC_OPERATOR_ALERTS = "operator-alerts";

    private final EmailService emailService;

    // Handles OperatorAlertEvent on operator-alerts topic
    @KafkaHandler
    public void handleOperatorAlertEvent(OperatorAlertEvent event) {
        log.warn("Received operator alert event for Execution ID {}: BusinessDate={}, FailureReason={}",
                event.executionId(),
                event.businessDate(),
                event.failureReason()
        );

        emailService.sendOperatorAlertEmail(event);
    }

    // Handles ProcessExecutionNotificationEvent on operator-alerts topic when sent there
    @KafkaHandler
    public void handleProcessExecutionNotificationEvent(ProcessExecutionNotificationEvent event) {
        log.info("Received process execution notification event on operator-alerts for Execution ID {}: Status={}, BusinessDate={}, TotalFiles={}, ProcessedFiles={}, FailedFiles={}",
                event.executionId(),
                event.status(),
                event.businessDate(),
                event.totalFilesCount(),
                event.processedFilesCount(),
                event.failedFilesCount()
        );

        emailService.sendProcessExecutionNotificationEmail(event);
    }

    // Safely catches and logs any other unrecognized message types published to operator-alerts
    @KafkaHandler(isDefault = true)
    public void handleUnknownPayload(Object unknownPayload) {
        log.warn("Received unexpected payload type on topic {}: {}",
                TOPIC_OPERATOR_ALERTS,
                unknownPayload != null ? unknownPayload.getClass().getName() : "null"
        );
    }
}