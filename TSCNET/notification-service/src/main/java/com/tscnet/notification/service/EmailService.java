package com.tscnet.notification.service;

import com.tscnet.notification.event.OperatorAlertEvent;
import com.tscnet.notification.event.ProcessExecutionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.recipient}")
    private String recipientEmail;

    public void sendProcessExecutionNotificationEmail(ProcessExecutionNotificationEvent event) {
        log.info("Preparing to send process summary email for Execution ID: {} | Status: {}",
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

        sendEmail(subject, body, event.executionId());
    }

    public void sendOperatorAlertEmail(OperatorAlertEvent event) {
        log.warn("Preparing to send operator alert email for Execution ID: {}", event.executionId());

        String subject = String.format("ALERT: Process Execution Failure - %s [Execution ID: %d]",
                event.businessDate(), event.executionId());

        String body = String.format("""
                OPERATOR ALERT - ACTION REQUIRED
                --------------------------------
                Execution ID: %d
                Business Date: %s
                Failure Reason: %s
                Timestamp: %s
                """,
                event.executionId(),
                event.businessDate(),
                event.failureReason(),
                event.timestamp()
        );

        sendEmail(subject, body, event.executionId());
    }

    private void sendEmail(String subject, String body, Long executionId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email successfully dispatched to {} for Execution ID: {}", recipientEmail, executionId);
        } catch (Exception e) {
            log.error("Failed to send email for Execution ID: {}", executionId, e);
        }
    }
}