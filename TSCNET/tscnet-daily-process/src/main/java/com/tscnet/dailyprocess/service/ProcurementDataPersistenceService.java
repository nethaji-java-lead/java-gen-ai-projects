package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcurementDataPersistenceService {

    private final ProcurementAssessmentService procurementAssessmentService;
    private final ProcurementOfferService procurementOfferService;

    @Transactional
    public void persistProcessedData(List<ProcurementOfferDTO> offers,
                                     ProcurementAssessmentDTO assessment,
                                     String fileName) {
        log.info("Persisting assessment and offers for file: {}", fileName);

        procurementAssessmentService.save(assessment, fileName);
        procurementOfferService.saveAll(offers, fileName);
    }
}