package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import com.tscnet.dailyprocess.model.AssessmentStatus;
import com.tscnet.dailyprocess.model.ProcurementAssessmentProperties;
import com.tscnet.dailyprocess.repository.ProcurementAssessmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcurementAssessmentServiceTest {

    @Mock
    private ProcurementAssessmentProperties properties;

    @Mock
    private ProcurementAssessmentRepository procurementAssessmentRepository;

    @InjectMocks
    private ProcurementAssessmentService assessmentService;

    private final LocalDate businessDate = LocalDate.of(2026, 9, 28);

    private ProcurementOfferDTO createOffer(BigDecimal quantity, BigDecimal price) {
        return new ProcurementOfferDTO(
                "MRID-001",
                "A01",
                "A01",
                "A01",
                "A04",
                "A01",
                "EUR",
                "MAW",
                1,
                quantity,
                price,
                "A01"
        );
    }

    @Test
    void procurementAssessment_ShouldReject_WhenOffersAreNullOrEmpty() {
        ProcurementAssessmentDTO nullResult = assessmentService.procurementAssessment(null, businessDate);
        assertEquals(AssessmentStatus.REJECT, nullResult.status());
        assertEquals("No procurement offers received", nullResult.reason());

        ProcurementAssessmentDTO emptyResult = assessmentService.procurementAssessment(Collections.emptyList(), businessDate);
        assertEquals(AssessmentStatus.REJECT, emptyResult.status());
        assertEquals("No procurement offers received", emptyResult.reason());
    }

    @Test
    void procurementAssessment_ShouldReview_WhenOffersAreIncomplete() {
        ProcurementOfferDTO incompleteOffer = createOffer(null, new BigDecimal("10.0"));
        List<ProcurementOfferDTO> offers = List.of(incompleteOffer);

        ProcurementAssessmentDTO result = assessmentService.procurementAssessment(offers, businessDate);

        assertEquals(AssessmentStatus.REVIEW, result.status());
        assertEquals("One or more procurement offers are incomplete", result.reason());
    }

    @Test
    void procurementAssessment_ShouldReview_WhenOfferCountBelowMinimum() {
        when(properties.minimumOffers()).thenReturn(3);

        List<ProcurementOfferDTO> offers = List.of(
                createOffer(new BigDecimal("100"), new BigDecimal("10.0")),
                createOffer(new BigDecimal("200"), new BigDecimal("12.0"))
        );

        ProcurementAssessmentDTO result = assessmentService.procurementAssessment(offers, businessDate);

        assertEquals(AssessmentStatus.REVIEW, result.status());
        assertEquals("Minimum number of procurement offers not received", result.reason());
    }

    @Test
    void procurementAssessment_ShouldReview_WhenWeightedPriceExceedsMaximumThreshold() {
        when(properties.minimumOffers()).thenReturn(2);
        when(properties.minimumQuantity()).thenReturn(new BigDecimal("300"));
        when(properties.maximumWeightedAveragePrice()).thenReturn(new BigDecimal("15.0000"));

        List<ProcurementOfferDTO> offers = List.of(
                createOffer(new BigDecimal("100"), new BigDecimal("20.0")),
                createOffer(new BigDecimal("200"), new BigDecimal("20.0"))
        );

        ProcurementAssessmentDTO result = assessmentService.procurementAssessment(offers, businessDate);

        assertEquals(AssessmentStatus.REVIEW, result.status());
        assertEquals("Weighted average procurement price exceeds the configured threshold", result.reason());
        assertFalse(result.priceThresholdMet());
    }

    @Test
    void procurementAssessment_ShouldAccept_WhenAllConditionsAreMet() {
        when(properties.minimumOffers()).thenReturn(2);
        when(properties.minimumQuantity()).thenReturn(new BigDecimal("300"));
        when(properties.maximumWeightedAveragePrice()).thenReturn(new BigDecimal("25.0000"));

        List<ProcurementOfferDTO> offers = List.of(
                createOffer(new BigDecimal("100"), new BigDecimal("10.0")),
                createOffer(new BigDecimal("200"), new BigDecimal("20.0"))
        );

        ProcurementAssessmentDTO result = assessmentService.procurementAssessment(offers, businessDate);

        assertEquals(AssessmentStatus.ACCEPT, result.status());
        assertEquals("Procurement data passed completeness, quantity and price assessment", result.reason());
        assertTrue(result.quantityThresholdMet());
        assertTrue(result.priceThresholdMet());
        assertEquals(new BigDecimal("300"), result.totalQuantity());
        assertEquals(new BigDecimal("16.6667"), result.weightedAveragePrice());
    }

    @Test
    void procurementAssessment_ShouldReview_WhenQuantityBelowMinimumThreshold() {
        when(properties.minimumOffers()).thenReturn(2);
        when(properties.minimumQuantity()).thenReturn(new BigDecimal("500"));
        when(properties.maximumWeightedAveragePrice()).thenReturn(new BigDecimal("25.0000")); // Added missing stub

        List<ProcurementOfferDTO> offers = List.of(
                createOffer(new BigDecimal("100"), new BigDecimal("10.0")),
                createOffer(new BigDecimal("200"), new BigDecimal("12.0"))
        );

        ProcurementAssessmentDTO result = assessmentService.procurementAssessment(offers, businessDate);

        assertEquals(AssessmentStatus.REVIEW, result.status());
        assertEquals("Total procurement quantity is below the configured minimum threshold", result.reason());
        assertFalse(result.quantityThresholdMet());
    }
}