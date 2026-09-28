package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import com.tscnet.dailyprocess.model.AssessmentStatus;
import com.tscnet.dailyprocess.model.ProcurementAssessmentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcurementAssessmentService {
    private final ProcurementAssessmentProperties properties;

    public ProcurementAssessmentDTO procurementAssessment(String documentMRID, List<ProcurementOfferDTO> offers) {

        // 1. Completeness Check
        if (offers == null || offers.isEmpty()) {
            log.warn("Procurement assessment failed: No offers received.");
            return new ProcurementAssessmentDTO(
                    BigDecimal.ZERO, BigDecimal.ZERO, false, false, false,
                    AssessmentStatus.REJECT, "No procurement offers received"
            );
        }

        boolean isCompleted = offers.stream().allMatch(this::isComplete);
        if (!isCompleted) {
            log.warn("Procurement assessment failed: Incomplete offer fields detected.");
            return new ProcurementAssessmentDTO(
                    BigDecimal.ZERO, BigDecimal.ZERO, true, false, false,
                    AssessmentStatus.REVIEW, "One or more procurement offers are incomplete"
            );
        }

        // 2. Minimum Offer Count
        if (offers.size() < properties.minimumOffers()) {
            log.warn("Procurement assessment failed: Offer count {} is below minimum {}",
                    offers.size(), properties.minimumOffers());
            return new ProcurementAssessmentDTO(
                    BigDecimal.ZERO, BigDecimal.ZERO, true, false, false,
                    AssessmentStatus.REVIEW, "Minimum number of procurement offers not received"
            );
        }

        // 3. Quantity Aggregation
        BigDecimal totalQuantity = offers.stream()
                .map(ProcurementOfferDTO::quantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Price Aggregation
        BigDecimal weightedPriceTotal = offers.stream()
                .map(offer -> offer.quantity().multiply(offer.procurementPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedAveragePrice = (totalQuantity.compareTo(BigDecimal.ZERO) <= 0)
                ? BigDecimal.ZERO
                : weightedPriceTotal.divide(totalQuantity, 4, RoundingMode.HALF_UP);

        // 5. Threshold Checks
        boolean quantityThresholdMet = totalQuantity.compareTo(properties.minimumQuantity()) >= 0;
        boolean priceThresholdMet = weightedAveragePrice.compareTo(properties.maximumWeightedAveragePrice()) <= 0;

        // 6. Final Assessment
        AssessmentStatus status;
        String reason;

        if (!quantityThresholdMet) {
            status = AssessmentStatus.REVIEW;
            reason = "Total procurement quantity is below the configured minimum threshold";
        } else if (!priceThresholdMet) {
            status = AssessmentStatus.REVIEW;
            reason = "Weighted average procurement price exceeds the configured threshold";
        } else {
            status = AssessmentStatus.ACCEPT;
            reason = "Procurement data passed completeness, quantity and price assessment";
        }

        ProcurementAssessmentDTO resultDTO = new ProcurementAssessmentDTO(
                totalQuantity,
                weightedAveragePrice,
                true,
                quantityThresholdMet,
                priceThresholdMet,
                status,
                reason
        );

        log.info("Procurement assessment completed. totalQuantity={}, weightedAveragePrice={}, status={}, reason={}",
                totalQuantity, weightedAveragePrice, status, reason);

        return resultDTO;
    }

    private boolean isComplete(ProcurementOfferDTO offer) {
        return Objects.nonNull(offer)
                && Objects.nonNull(offer.quantity())
                && Objects.nonNull(offer.procurementPrice());
    }
}