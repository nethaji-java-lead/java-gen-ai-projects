package com.tscnet.notification.service;

import com.tscnet.notification.event.ProcessExecutionNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendProcessExecutionNotificationEmail(ProcessExecutionNotificationEvent event) {
        log.info("Sending process summary email for Execution ID: {} | Status: {}",
                event.executionId(), event.status());

        String subject = String.format("Process Execution Report - %s [Status: %s]",
                event.businessDate(), event.status());

        String body = String.format("""
                Process Execution Summary:
                ---------------------------
                Execution ID: %d
                Business Date: %s
                Initiation Type: %s
                Triggered By: %s
                Final Status: %s
                Total Files: %d
                Processed Files: %d
                Failed Files: %d
                Timestamp: %s
                """,
                event.executionId(),
                event.businessDate(),
                event.initiationType(),
                event.triggeredBy(),
                event.status(),
                event.totalFilesCount(),
                event.processedFilesCount(),
                event.failedFilesCount(),
                event.timestamp()
        );

        // Logic to dispatch email via JavaMailSender / SMTP...
    }
}