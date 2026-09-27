package com.tscnet.dailyprocess.repository;

import com.tscnet.dailyprocess.model.ProcessExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessExecutionLogRepository extends JpaRepository<ProcessExecutionLog, Long> {

    Optional<ProcessExecutionLog> findByBusinessDate(LocalDate date);
    List<ProcessExecutionLog> findTop50ByOrderByStartTimeDesc();
}
