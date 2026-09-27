package com.tscnet.dailyprocess.repository;
import com.tscnet.dailyprocess.model.ProcessExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface ProcessExecutionLogRepository extends JpaRepository<ProcessExecutionLog,Long> {
    Optional<ProcessExecutionLog> findByBusinessDate(LocalDate date);
    List<ProcessExecutionLog> findTop50ByOrderByStartTimeDesc();
}
