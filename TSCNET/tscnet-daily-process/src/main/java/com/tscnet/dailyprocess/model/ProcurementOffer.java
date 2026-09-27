package com.tscnet.dailyprocess.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "m_rid")
    private String mRID;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "market_agreement_type")
    private String marketAgreementType;

    @Column(name = "original_market_product_type")
    private String originalMarketProductType;

    @Column(name = "psr_type")
    private String psrType;

    @Column(name = "flow_direction")
    private String flowDirection;

    @Column(name = "currency_unit")
    private String currencyUnit;

    @Column(name = "quantity_measure_unit")
    private String quantityMeasureUnit;

    @Column(name = "position")
    private Integer position;

    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "procurement_price", precision = 18, scale = 4)
    private BigDecimal procurementPrice;

    @Column(name = "imbalance_price_category")
    private String imbalancePriceCategory;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}