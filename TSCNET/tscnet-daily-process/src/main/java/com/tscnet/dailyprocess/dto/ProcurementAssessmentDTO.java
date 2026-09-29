package com.tscnet.dailyprocess.dto;

import com.tscnet.dailyprocess.event.ProcurementAssessmentEvent;
import com.tscnet.dailyprocess.model.AssessmentStatus;
import com.tscnet.dailyprocess.model.ProcurementAssessment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ProcurementAssessmentDTO(
        BigDecimal totalQuantity,
        BigDecimal weightedAveragePrice,
        boolean complete,
        boolean quantityThresholdMet,
        boolean priceThresholdMet,
        AssessmentStatus status,
        String reason,
        LocalDate businessDate
) {

    public ProcurementAssessment toProcurementAssessmentEntity(String fileName) {
        return ProcurementAssessment.builder()
                .totalQuantity(this.totalQuantity)
                .weightedAveragePrice((this.weightedAveragePrice))
                .complete(this.complete)
                .quantityThresholdMet(this.quantityThresholdMet)
                .priceThresholdMet(this.priceThresholdMet)
                .status(this.status)
                .reason(this.reason)
                .fileName(fileName)
                .businessDate(this.businessDate)
                .build();
    }
}