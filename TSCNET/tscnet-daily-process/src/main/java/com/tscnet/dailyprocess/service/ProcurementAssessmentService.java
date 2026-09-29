package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import com.tscnet.dailyprocess.model.AssessmentStatus;
import com.tscnet.dailyprocess.model.ProcurementAssessment;
import com.tscnet.dailyprocess.model.ProcurementAssessmentProperties;
import com.tscnet.dailyprocess.repository.ProcurementAssessmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcurementAssessmentService {

    private final ProcurementAssessmentProperties properties;
    private final ProcurementAssessmentRepository procurementAssessmentRepository;

    public ProcurementAssessmentDTO procurementAssessment(List<ProcurementOfferDTO> offers, LocalDate businessDate) {
        LocalDate effectiveBusinessDate = (businessDate != null) ? businessDate : LocalDate.now();

        if (offers == null || offers.isEmpty()) {
            log.warn("Procurement assessment failed: No offers received.");
            return new ProcurementAssessmentDTO(
                    BigDecimal.ZERO, BigDecimal.ZERO, false, false, false,
                    AssessmentStatus.REJECT, "No procurement offers received", effectiveBusinessDate
            );
        }

        boolean isCompleted = offers.stream().allMatch(this::isComplete);
        if (!isCompleted) {
            log.warn("Procurement assessment failed: Incomplete offer fields detected.");
            return new ProcurementAssessmentDTO(
                    BigDecimal.ZERO, BigDecimal.ZERO, true, false, false,
                    AssessmentStatus.REVIEW, "One or more procurement offers are incomplete", effectiveBusinessDate
            );
        }

        if (offers.size() < properties.minimumOffers()) {
            log.warn("Procurement assessment failed: Offer count {} is below minimum {}",
                    offers.size(), properties.minimumOffers());
            return new ProcurementAssessmentDTO(
                    BigDecimal.ZERO, BigDecimal.ZERO, true, false, false,
                    AssessmentStatus.REVIEW, "Minimum number of procurement offers not received", effectiveBusinessDate
            );
        }

        BigDecimal totalQuantity = offers.stream()
                .map(ProcurementOfferDTO::quantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedPriceTotal = offers.stream()
                .map(offer -> offer.quantity().multiply(offer.procurementPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedAveragePrice = (totalQuantity.compareTo(BigDecimal.ZERO) <= 0)
                ? BigDecimal.ZERO
                : weightedPriceTotal.divide(totalQuantity, 4, RoundingMode.HALF_UP);

        boolean quantityThresholdMet = totalQuantity.compareTo(properties.minimumQuantity()) >= 0;
        boolean priceThresholdMet = weightedAveragePrice.compareTo(properties.maximumWeightedAveragePrice()) <= 0;

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
                reason,
                effectiveBusinessDate
        );

        log.info("Procurement assessment completed. totalQuantity={}, weightedAveragePrice={}, status={}, reason={}, businessDate={}",
                totalQuantity, weightedAveragePrice, status, reason, effectiveBusinessDate);

        return resultDTO;
    }

    @Transactional
    public ProcurementAssessment save(ProcurementAssessmentDTO dto, String fileName) {
        if (dto == null) {
            return null;
        }
        ProcurementAssessment entity = dto.toProcurementAssessmentEntity(fileName);
        return procurementAssessmentRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ProcurementAssessmentDTO> findByBusinessDate(LocalDate businessDate) {
        return procurementAssessmentRepository.findByBusinessDate(businessDate)
                .stream()
                .map(entity -> new ProcurementAssessmentDTO(
                        entity.getTotalQuantity(),
                        entity.getWeightedAveragePrice(),
                        entity.isComplete(),
                        entity.isQuantityThresholdMet(),
                        entity.isPriceThresholdMet(),
                        entity.getStatus(),
                        entity.getReason(),
                        entity.getBusinessDate()
                ))
                .toList();
    }

    private boolean isComplete(ProcurementOfferDTO offer) {
        return Objects.nonNull(offer)
                && Objects.nonNull(offer.quantity())
                && Objects.nonNull(offer.procurementPrice());
    }
}