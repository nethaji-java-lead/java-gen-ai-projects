package com.tscnet.dailyprocess.dto;

import com.tscnet.dailyprocess.model.ProcurementOffer;
import lombok.ToString;

import java.math.BigDecimal;

public record ProcurementOfferDTO(
        String mRID,
        String businessType,
        String marketAgreementType,
        String originalMarketProductType,
        String psrType,
        String flowDirection,
        String currencyUnit,
        String quantityMeasureUnit,
        int position,
        BigDecimal quantity,
        BigDecimal procurementPrice,
        String imbalancePriceCategory
) {

    public ProcurementOffer toProcurementOfferEntity(String fileName) {
        return ProcurementOffer.builder()
                .mRID(this.mRID)
                .businessType(this.businessType)
                .marketAgreementType(this.marketAgreementType)
                .originalMarketProductType(this.originalMarketProductType)
                .psrType(this.psrType)
                .flowDirection(this.flowDirection)
                .currencyUnit(this.currencyUnit)
                .quantityMeasureUnit(this.quantityMeasureUnit)
                .position(this.position)
                .quantity(this.quantity)
                .procurementPrice(this.procurementPrice)
                .imbalancePriceCategory(this.imbalancePriceCategory)
                .fileName(fileName)
                .build();
    }

    @Override
    public String toString() {
        return "ProcurementOfferDTO{" +
                "mRID='" + mRID + '\'' +
                ", businessType='" + businessType + '\'' +
                ", marketAgreementType='" + marketAgreementType + '\'' +
                ", originalMarketProductType='" + originalMarketProductType + '\'' +
                ", psrType='" + psrType + '\'' +
                ", flowDirection='" + flowDirection + '\'' +
                ", currencyUnit='" + currencyUnit + '\'' +
                ", quantityMeasureUnit='" + quantityMeasureUnit + '\'' +
                ", position=" + position +
                ", quantity=" + quantity +
                ", procurementPrice=" + procurementPrice +
                ", imbalancePriceCategory='" + imbalancePriceCategory + '\'' +
                '}';
    }
}