package com.tscnet.dailyprocess.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_quantity", precision = 18, scale = 4)
    private BigDecimal totalQuantity;

    @Column(name = "weighted_average_price", precision = 18, scale = 4)
    private BigDecimal weightedAveragePrice;

    @Column(name = "is_complete")
    private boolean complete;

    @Column(name = "quantity_threshold_met")
    private boolean quantityThresholdMet;

    @Column(name = "price_threshold_met")
    private boolean priceThresholdMet;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssessmentStatus status;

    @Column(name = "reason")
    private String reason;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}