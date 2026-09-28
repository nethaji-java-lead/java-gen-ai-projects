package com.tscnet.dailyprocess.repository;

import com.tscnet.dailyprocess.model.ExecutionStatus;
import com.tscnet.dailyprocess.model.ProcessExecutionLog;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessExecutionLogRepository extends JpaRepository<ProcessExecutionLog, Long> {

    // Option A: Find the most recent record for today
    @EntityGraph(attributePaths = "fileLogs")
    Optional<ProcessExecutionLog> findFirstByBusinessDateOrderByStartTimeDesc(LocalDate businessDate);

    // Option B: Find all records for today
    List<ProcessExecutionLog> findByBusinessDate(LocalDate businessDate);

    @EntityGraph(attributePaths = "fileLogs")
    Optional<ProcessExecutionLog> findById(@NonNull Long id);

    @EntityGraph(attributePaths = "fileLogs")
    List<ProcessExecutionLog> findByStatusIn(List<ExecutionStatus> statuses);
}
