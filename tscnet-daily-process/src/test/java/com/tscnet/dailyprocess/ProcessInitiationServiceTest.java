package com.tscnet.dailyprocess;

import com.tscnet.dailyprocess.model.ExecutionStatus;
import com.tscnet.dailyprocess.model.InitiationType;
import com.tscnet.dailyprocess.model.ProcessStatus;
import com.tscnet.dailyprocess.repository.DailyProcessRepository;
import com.tscnet.dailyprocess.repository.ProcessExecutionRepository;
import com.tscnet.dailyprocess.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ProcessInitiationServiceTest {

    private ProcessInitiationService service;
    private ProcessExecutionRepository executions;
    private DailyProcessRepository processes;

    @BeforeEach
    void setUp() {
        executions = new InMemoryExecutionRepository();
        processes = new InMemoryProcessRepository();

        service = new ProcessInitiationService(
                executions,
                processes,
                new BusinessCalendarService(),
                new NotificationService(),
                Clock.fixed(Instant.parse("2026-09-25T00:01:00Z"), ZoneOffset.UTC));
    }

    @Test
    void successfulAutomaticInitiationMakesProcessAvailable() {
        LocalDate date = LocalDate.of(2026, 9, 25);

        var result = service.automaticInitiation(date);

        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertEquals(InitiationType.AUTOMATIC, result.getInitiationType());
        assertEquals(ProcessStatus.AVAILABLE,
                processes.findByBusinessDate(date).orElseThrow().getStatus());
    }

    @Test
    void failedAutomaticInitiationIsRecorded() {
        LocalDate date = LocalDate.of(2026, 9, 25);

        var result = service.automaticInitiation(date);

        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertEquals("Simulated downstream initiation failure", result.getFailureReason());
        assertTrue(executions.findByBusinessDateOrderByInitiatedAtDesc(date).size() == 1);
    }

    @Test
    void manualInitiationCanRecoverFromAutomaticFailure() {
        LocalDate date = LocalDate.of(2026, 9, 25);

        var failed = service.automaticInitiation(date);
        assertEquals(ExecutionStatus.FAILED, failed.getStatus());

        var manual = service.manualInitiation(date, "operator-1");

        assertEquals(ExecutionStatus.SUCCESS, manual.getStatus());
        assertEquals(InitiationType.MANUAL, manual.getInitiationType());
        assertEquals(ProcessStatus.AVAILABLE,
                processes.findByBusinessDate(date).orElseThrow().getStatus());
        assertEquals(2, executions.findByBusinessDateOrderByInitiatedAtDesc(date).size());
    }

    @Test
    void weekendIsNotAValidBusinessDay() {
        LocalDate saturday = LocalDate.of(2026, 9, 26);

        var result = service.automaticInitiation(saturday);

        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertEquals("Not a business day", result.getFailureReason());
    }
}
