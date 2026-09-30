package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcurementDataPersistenceServiceTest {

    @Mock
    private ProcurementAssessmentService procurementAssessmentService;

    @Mock
    private ProcurementOfferService procurementOfferService;

    @InjectMocks
    private ProcurementDataPersistenceService persistenceService;

    @Test
    void persistProcessedData_ShouldSaveAssessmentAndOffers() {
        ProcurementOfferDTO offerDTO = mock(ProcurementOfferDTO.class);
        ProcurementAssessmentDTO assessmentDTO = mock(ProcurementAssessmentDTO.class);
        String fileName = "test.xml";

        persistenceService.persistProcessedData(List.of(offerDTO), assessmentDTO, fileName);

        verify(procurementAssessmentService, times(1)).save(assessmentDTO, fileName);
        verify(procurementOfferService, times(1)).saveAll(List.of(offerDTO), fileName);
    }
}