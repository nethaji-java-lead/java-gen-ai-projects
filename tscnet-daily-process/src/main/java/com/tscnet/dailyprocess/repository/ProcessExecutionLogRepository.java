package com.tscnet.dailyprocess.repository;

import com.tscnet.dailyprocess.model.ProcessExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessExecutionLogRepository extends JpaRepository<ProcessExecutionLog, Long> {
}
