package com.tscnet.dailyprocess.repository;

import com.tscnet.dailyprocess.model.DailyProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyProcessRepository extends JpaRepository<DailyProcess, Long> {
    Optional<DailyProcess> findByBusinessDate(LocalDate businessDate);
}
