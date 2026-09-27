package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import com.tscnet.dailyprocess.model.ProcurementAssessment;
import com.tscnet.dailyprocess.model.ProcurementOffer;
import com.tscnet.dailyprocess.model.xmlelement.BalancingMarketDocument;
import com.tscnet.dailyprocess.repository.ProcurementAssessmentRepository;
import com.tscnet.dailyprocess.repository.ProcurementOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcurementDataPersistenceService {

    private final ProcurementAssessmentRepository procurementAssessmentRepository;
    private final ProcurementOfferRepository procurementOfferRepository;

    @Transactional
    public void persistProcessedData(BalancingMarketDocument document,
                                     List<ProcurementOfferDTO> offers,
                                     ProcurementAssessmentDTO assessment,
                                     String fileName) {
        log.info("Persisting assessment and offers for file: {}", fileName);

        // 1. Map ProcurementAssessment record/DTO to Entity
        ProcurementAssessment assessmentEntity = assessment.toProcurementOfferEntity(fileName);
        procurementAssessmentRepository.save(assessmentEntity);

        // 2. Map List<ProcurementOfferDTO> to List<ProcurementOffer>
        List<ProcurementOffer> offerEntities = offers.stream()
                .map(offer -> offer.toProcurementOfferEntity(fileName))
                .toList();

        procurementOfferRepository.saveAll(offerEntities);
    }
}