package com.tscnet.dailyprocess.event;

import java.time.Instant;
import java.time.LocalDate;

public record OperatorAlertEvent(
        Long executionId,
        LocalDate businessDate,
        String failureReason,
        Instant timestamp
) {}