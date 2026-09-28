package com.tscnet.dailyprocess.dto;

import com.tscnet.dailyprocess.event.ProcurementAssessmentEvent;
import com.tscnet.dailyprocess.model.AssessmentStatus;
import com.tscnet.dailyprocess.model.ProcurementAssessment;

import java.math.BigDecimal;
import java.time.Instant;

public record ProcurementAssessmentDTO(
        BigDecimal totalQuantity,
        BigDecimal weightedAveragePrice,
        boolean complete,
        boolean quantityThresholdMet,
        boolean priceThresholdMet,
        AssessmentStatus status,
        String reason
) {

    public ProcurementAssessment toProcurementOfferEntity(String fileName) {
        return ProcurementAssessment.builder()
                .totalQuantity(this.totalQuantity)
                .weightedAveragePrice((this.weightedAveragePrice))
                .complete(this.complete)
                .quantityThresholdMet(this.quantityThresholdMet)
                .priceThresholdMet(this.priceThresholdMet)
                .status(this.status)
                .reason(this.reason)
                .fileName(fileName)
                .build();
    }

    public ProcurementAssessmentEvent getProcurementAssessmentEvent(String documentMRID)
    {
        return new ProcurementAssessmentEvent(
                documentMRID,
                this.totalQuantity(),
                this.weightedAveragePrice(),
                this.status(),
                this.reason(),
                Instant.now()
        );
    }
}