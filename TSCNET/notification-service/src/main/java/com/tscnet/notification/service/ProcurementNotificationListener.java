package com.tscnet.notification.service;

import com.tscnet.notification.event.ProcessExecutionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcurementNotificationListener {

    private static final String GROUP_ID = "notification-service-group";
    private static final String TOPIC_PROCUREMENT_NOTIFICATIONS = "procurement-assessment-notifications";

    private final EmailService emailService;

    @KafkaListener(topics = TOPIC_PROCUREMENT_NOTIFICATIONS, groupId = GROUP_ID)
    public void handleProcessExecutionNotificationEvent(ProcessExecutionNotificationEvent event) {
        log.info("Received process execution notification event for Execution ID {}: Status={}, BusinessDate={}, TotalFiles={}, ProcessedFiles={}, FailedFiles={}",
                event.executionId(),
                event.status(),
                event.businessDate(),
                event.totalFilesCount(),
                event.processedFilesCount(),
                event.failedFilesCount()
        );

        // Dispatch Email with full execution context
        emailService.sendProcessExecutionNotificationEmail(event);
    }
}