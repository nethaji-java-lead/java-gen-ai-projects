package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.model.*;
import com.tscnet.dailyprocess.repository.DailyProcessRepository;
import com.tscnet.dailyprocess.repository.ProcessExecutionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProcessInitiationService {


    @Value("${app.simulate.failure:false}")
    private boolean shouldSimulateFailure;

    private final ProcessExecutionRepository executionRepository;
    private final DailyProcessRepository processRepository;
    private final BusinessCalendarService calendarService;
    private final NotificationService notificationService;
    private final Clock clock;

    public ProcessInitiationService(ProcessExecutionRepository executionRepository, DailyProcessRepository processRepository, BusinessCalendarService calendarService, NotificationService notificationService, Clock clock) {
        this.executionRepository = executionRepository;
        this.processRepository = processRepository;
        this.calendarService = calendarService;
        this.notificationService = notificationService;
        this.clock = clock;
    }


    @Transactional
    public ProcessExecution automaticInitiation(LocalDate businessDate) {
        if (!calendarService.isBusinessDay(businessDate)) {
            return recordFailure(businessDate, InitiationType.AUTOMATIC, "SYSTEM",
                    "Not a business day");
        }

        // Idempotency: if the process is already available, don't create another process.
        if (processRepository.findByBusinessDate(businessDate)
                .map(p -> p.getStatus() == ProcessStatus.AVAILABLE)
                .orElse(false)) {
            return recordFailure(businessDate, InitiationType.AUTOMATIC, "SYSTEM",
                    "Process is already available for this business date");
        }

        return initiate(businessDate, InitiationType.AUTOMATIC, "SYSTEM");
    }

    @Transactional
    public ProcessExecution manualInitiation(LocalDate businessDate, String operator) {
        if (!calendarService.isBusinessDay(businessDate)) {
            throw new IllegalArgumentException("Cannot manually initiate a non-business day");
        }

        return initiate(businessDate, InitiationType.MANUAL, operator);
    }

    private ProcessExecution initiate(
            LocalDate businessDate,
            InitiationType type,
            String initiatedBy) {

        Instant now = Instant.now(clock);

        ProcessExecution execution = new ProcessExecution();
        execution.setBusinessDate(businessDate);
        execution.setInitiationType(type);
        execution.setInitiatedBy(initiatedBy);
        execution.setInitiatedAt(now);

        try {
            if (shouldSimulateFailure) {
                throw new IllegalStateException("Simulated downstream initiation failure");
            }

            DailyProcess process = processRepository.findByBusinessDate(businessDate)
                    .orElseGet(() -> {
                        DailyProcess p = new DailyProcess();
                        p.setBusinessDate(businessDate);
                        return p;
                    });

            process.setStatus(ProcessStatus.AVAILABLE);
            process.setAvailableAt(now);
            processRepository.save(process);

            execution.setStatus(ExecutionStatus.SUCCESS);
            execution.setCompletedAt(Instant.now(clock));
            executionRepository.save(execution);

            notificationService.notifyParticipants(
                    "Daily process is available for " + businessDate);

            return execution;
        } catch (Exception ex) {
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setFailureReason(ex.getMessage());
            execution.setCompletedAt(Instant.now(clock));
            executionRepository.save(execution);

            if (type == InitiationType.AUTOMATIC) {
                notificationService.notifyOperator(
                        "Automatic initiation failed for " + businessDate + ": " + ex.getMessage());
            }

            return execution;
        }
    }

    private ProcessExecution recordFailure(
            LocalDate date, InitiationType type, String initiatedBy, String reason) {
        ProcessExecution execution = new ProcessExecution();
        execution.setBusinessDate(date);
        execution.setInitiationType(type);
        execution.setInitiatedBy(initiatedBy);
        execution.setInitiatedAt(Instant.now(clock));
        execution.setCompletedAt(Instant.now(clock));
        execution.setStatus(ExecutionStatus.FAILED);
        execution.setFailureReason(reason);
        return executionRepository.save(execution);
    }

    public List<ProcessExecution> history() {
        return executionRepository.findAllByOrderByInitiatedAtDesc();
    }

    public List<ProcessExecution> history(LocalDate businessDate) {
        return executionRepository.findByBusinessDateOrderByInitiatedAtDesc(businessDate);
    }
}
