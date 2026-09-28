package com.tscnet.dailyprocess.event;

import com.tscnet.dailyprocess.model.ExecutionStatus;
import com.tscnet.dailyprocess.model.InitiationType;

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