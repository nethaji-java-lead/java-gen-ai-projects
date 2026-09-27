package com.tscnet.dailyprocess.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "procurement.assessment")
public record ProcurementAssessmentProperties(
        BigDecimal minimumQuantity,
        BigDecimal maximumWeightedAveragePrice,
        int minimumOffers
) {}