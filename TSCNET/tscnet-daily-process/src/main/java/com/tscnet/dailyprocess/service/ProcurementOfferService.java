package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import com.tscnet.dailyprocess.model.ProcurementOffer;
import com.tscnet.dailyprocess.repository.ProcurementOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ProcurementOfferService {

    private final ProcurementOfferRepository repository;

    public ProcurementOfferService(ProcurementOfferRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<ProcurementOffer> saveAll(List<ProcurementOfferDTO> dtos, String fileName) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        List<ProcurementOffer> entities = dtos.stream()
                .map(dto -> dto.toProcurementOfferEntity(fileName))
                .toList();

        return repository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public List<ProcurementOfferDTO> findByBusinessDate(LocalDate businessDate) {
        LocalDate targetDate = (businessDate != null) ? businessDate : LocalDate.now();
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        return repository.findByCreatedAtBetween(startOfDay, endOfDay)
                .stream()
                .map(entity -> new ProcurementOfferDTO(
                        entity.getMRID(),
                        entity.getBusinessType(),
                        entity.getMarketAgreementType(),
                        entity.getOriginalMarketProductType(),
                        entity.getPsrType(),
                        entity.getFlowDirection(),
                        entity.getCurrencyUnit(),
                        entity.getQuantityMeasureUnit(),
                        entity.getPosition() != null ? entity.getPosition() : 0,
                        entity.getQuantity(),
                        entity.getProcurementPrice(),
                        entity.getImbalancePriceCategory()
                ))
                .toList();
    }
}