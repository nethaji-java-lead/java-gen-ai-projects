package com.tscnet.dailyprocess.repository;

import com.tscnet.dailyprocess.model.ProcessExecution;
import com.tscnet.dailyprocess.model.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ProcessExecutionRepository extends JpaRepository<ProcessExecution, Long> {
    List<ProcessExecution> findAllByOrderByInitiatedAtDesc();
    List<ProcessExecution> findByBusinessDateOrderByInitiatedAtDesc(LocalDate businessDate);
    List<ProcessExecution> findByBusinessDateAndStatusOrderByInitiatedAtDesc(
            LocalDate businessDate, ExecutionStatus status);
}
