package com.tscnet.dailyprocess.model.xmlelement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class TimeSeries {

    private static final String NAMESPACE =
            "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4";

    @XmlElement(name = "mRID", namespace = NAMESPACE)
    private String mRID;

    @XmlElement(name = "businessType", namespace = NAMESPACE)
    private String businessType;

    @XmlElement(name = "type_MarketAgreement.type", namespace = NAMESPACE)
    private String marketAgreementType;

    @XmlElement(name = "original_MarketProduct.marketProductType", namespace = NAMESPACE)
    private String originalMarketProductType;

    @XmlElement(name = "mktPSRType.psrType", namespace = NAMESPACE)
    private String psrType;

    @XmlElement(name = "flowDirection.direction", namespace = NAMESPACE)
    private String flowDirection;

    @XmlElement(name = "currency_Unit.name", namespace = NAMESPACE)
    private String currencyUnit;

    @XmlElement(name = "quantity_Measure_Unit.name", namespace = NAMESPACE)
    private String quantityMeasureUnit;

    @XmlElement(name = "curveType", namespace = NAMESPACE)
    private String curveType;

    @XmlElement(name = "Period", namespace = NAMESPACE)
    private Period period;


    @Getter
    @Setter
    @ToString
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Period {

        @XmlElement(name = "resolution", namespace = NAMESPACE)
        private String resolution;

        @XmlElement(name = "Point", namespace = NAMESPACE)
        private List<Point> points;
    }


    @Getter
    @Setter
    @ToString
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Point {

        @XmlElement(name = "position", namespace = NAMESPACE)
        private int position;

        @XmlElement(name = "quantity", namespace = NAMESPACE)
        private BigDecimal quantity;

        @XmlElement(name = "procurement_Price.amount", namespace = NAMESPACE)
        private BigDecimal procurementPrice;

        @XmlElement(name = "imbalance_Price.category", namespace = NAMESPACE)
        private String imbalancePriceCategory;
    }
}