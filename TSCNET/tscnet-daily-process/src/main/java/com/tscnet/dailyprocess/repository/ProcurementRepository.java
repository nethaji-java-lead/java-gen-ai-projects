package com.tscnet.dailyprocess.repository;

import com.tscnet.dailyprocess.model.ProcurementAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProcurementRepository extends JpaRepository<ProcurementAssessment, Long> {

    /**
     * Find all procurement records for a given business date
     */
    List<ProcurementAssessment> findByBusinessDate(LocalDate businessDate);
}