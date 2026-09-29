package com.tscnet.dailyprocess.repository;

import com.tscnet.dailyprocess.model.ProcurementOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcurementOfferRepository extends JpaRepository<ProcurementOffer, Long> {

    /**
     * Find all offers created within a given date-time range.
     */
    List<ProcurementOffer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}