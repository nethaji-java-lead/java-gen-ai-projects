package com.tscnet.notification.event;

import com.tscnet.notification.model.ExecutionStatus;
import com.tscnet.notification.model.InitiationType;

import java.time.Instant;
import java.time.LocalDate;

public record ProcessExecutionNotificationEvent(
        Long executionId,
        LocalDate businessDate,
        InitiationType initiationType,
        ExecutionStatus status,
        int totalFilesCount,
        int processedFilesCount,
        int failedFilesCount,
        String triggeredBy,
        Instant timestamp
) {}