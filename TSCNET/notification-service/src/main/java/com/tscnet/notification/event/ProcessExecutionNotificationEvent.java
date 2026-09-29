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
        Integer totalFilesCount,
        Integer processedFilesCount,
        Integer failedFilesCount,
        String triggeredBy,
        Instant timestamp
) {}