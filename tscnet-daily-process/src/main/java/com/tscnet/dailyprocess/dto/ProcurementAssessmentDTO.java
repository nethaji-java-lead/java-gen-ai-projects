package com.tscnet.dailyprocess.model;

import java.math.BigDecimal;

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
}