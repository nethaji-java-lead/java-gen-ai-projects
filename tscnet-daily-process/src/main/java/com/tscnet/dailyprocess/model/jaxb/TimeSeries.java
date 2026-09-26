package com.tscnet.dailyprocess.model.jaxb;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimeSeries {

    @XmlElement(namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String mRID;

    @XmlElement(namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String businessType;

    @XmlElement(name = "type_MarketAgreement.type", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String marketAgreementType;

    @XmlElement(name = "original_MarketProduct.marketProductType", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String originalMarketProductType;

    @XmlElement(name = "mktPSRType.psrType", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String psrType;

    @XmlElement(name = "flowDirection.direction", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String flowDirection;

    @XmlElement(name = "currency_Unit.name", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String currencyUnit;

    @XmlElement(name = "quantity_Measure_Unit.name", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String quantityMeasureUnit;

    @XmlElement(namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String curveType;

    @XmlElement(name = "Period", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private Period period;

    // Getters and Setters
    public String getMRID() { return mRID; }
    public void setMRID(String mRID) { this.mRID = mRID; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getMarketAgreementType() { return marketAgreementType; }
    public void setMarketAgreementType(String marketAgreementType) { this.marketAgreementType = marketAgreementType; }

    public String getOriginalMarketProductType() { return originalMarketProductType; }
    public void setOriginalMarketProductType(String originalMarketProductType) { this.originalMarketProductType = originalMarketProductType; }

    public String getPsrType() { return psrType; }
    public void setPsrType(String psrType) { this.psrType = psrType; }

    public String getFlowDirection() { return flowDirection; }
    public void setFlowDirection(String flowDirection) { this.flowDirection = flowDirection; }

    public String getCurrencyUnit() { return currencyUnit; }
    public void setCurrencyUnit(String currencyUnit) { this.currencyUnit = currencyUnit; }

    public String getQuantityMeasureUnit() { return quantityMeasureUnit; }
    public void setQuantityMeasureUnit(String quantityMeasureUnit) { this.quantityMeasureUnit = quantityMeasureUnit; }

    public String getCurveType() { return curveType; }
    public void setCurveType(String curveType) { this.curveType = curveType; }

    public Period getPeriod() { return period; }
    public void setPeriod(Period period) { this.period = period; }

    // Inner Class: Period
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Period {

        @XmlElement(name = "resolution", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
        private String resolution;

        @XmlElement(name = "Point", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
        private List<Point> points;

        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }

        public List<Point> getPoints() { return points; }
        public void setPoints(List<Point> points) { this.points = points; }
    }

    // Inner Class: Point
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Point {

        @XmlElement(namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
        private int position;

        @XmlElement(namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
        private BigDecimal quantity;

        @XmlElement(name = "procurement_Price.amount", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
        private BigDecimal procurementPrice;

        @XmlElement(name = "imbalance_Price.category", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
        private String imbalancePriceCategory;

        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getProcurementPrice() { return procurementPrice; }
        public void setProcurementPrice(BigDecimal procurementPrice) { this.procurementPrice = procurementPrice; }

        public String getImbalancePriceCategory() { return imbalancePriceCategory; }
        public void setImbalancePriceCategory(String imbalancePriceCategory) { this.imbalancePriceCategory = imbalancePriceCategory; }
    }
}