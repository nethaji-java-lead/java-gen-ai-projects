package com.tscnet.dailyprocess.event;

import com.tscnet.dailyprocess.model.AssessmentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ProcurementAssessmentEvent(
        String documentMRID,
        BigDecimal totalQuantity,
        BigDecimal weightedAveragePrice,
        AssessmentStatus status,
        String reason,
        Instant timestamp
) {}