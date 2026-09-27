package com.tscnet.dailyprocess.model.jaxb;

import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "Balancing_MarketDocument", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@ToString
public class BalancingMarketDocument {

    private static final String NAMESPACE = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4";

    @XmlElement(namespace = NAMESPACE)
    private String mRID;

    @XmlElement(namespace = NAMESPACE)
    private int revisionNumber;

    @XmlElement(namespace = NAMESPACE)
    private String type;

    @XmlElement(name = "TimeSeries", namespace = NAMESPACE)
    private List<TimeSeries> timeSeries;

    public List<ProcurementOfferDTO> extractProcurementOffer() {
        if (timeSeries == null || timeSeries.isEmpty()) {
            return List.of();
        }

        return timeSeries.stream()
                .filter(Objects::nonNull)
                .map(this::toProcurementOfferDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    private ProcurementOfferDTO toProcurementOfferDTO(TimeSeries ts) {
        if (ts.getPeriod() == null || ts.getPeriod().getPoints() == null || ts.getPeriod().getPoints().isEmpty()) {
            return null;
        }

        List<TimeSeries.Point> points = ts.getPeriod().getPoints().stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getQuantity() != null)
                .toList();

        if (points.isEmpty()) {
            return null;
        }

        // 1. Calculate raw sum of point quantities (MW)
        BigDecimal rawMwSum = points.stream()
                .map(TimeSeries.Point::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Convert MW to MWh for 15-minute intervals (PT15M)
        String resolution = ts.getPeriod().getResolution();
        BigDecimal offerQuantity = "PT15M".equalsIgnoreCase(resolution)
                ? rawMwSum.divide(BigDecimal.valueOf(4), 4, RoundingMode.HALF_UP)
                : rawMwSum;

        // 3. Calculate weighted average price = SUM(quantity * price) / SUM(quantity)
        BigDecimal weightedPriceTotal = points.stream()
                .map(p -> {
                    BigDecimal qty = p.getQuantity();
                    BigDecimal price = p.getProcurementPrice() != null ? p.getProcurementPrice() : BigDecimal.ZERO;
                    return qty.multiply(price);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedPrice = rawMwSum.compareTo(BigDecimal.ZERO) > 0
                ? weightedPriceTotal.divide(rawMwSum, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Extract metadata from first valid point
        TimeSeries.Point firstPoint = points.get(0);

        return new ProcurementOfferDTO(
                ts.getMRID(),
                ts.getBusinessType(),
                ts.getMarketAgreementType(),
                ts.getOriginalMarketProductType(),
                ts.getPsrType(),
                ts.getFlowDirection(),
                ts.getCurrencyUnit(),
                ts.getQuantityMeasureUnit(),
                firstPoint.getPosition(),
                offerQuantity,
                weightedPrice,
                firstPoint.getImbalancePriceCategory()
        );
    }
}