package com.tscnet.dailyprocess.controller;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.exception.NonBusinessDayException;
import com.tscnet.dailyprocess.model.InitiationType;
import com.tscnet.dailyprocess.service.DateService;
import com.tscnet.dailyprocess.service.ProcessExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/process")
@Slf4j
public class ProcessController {

    public static final String INITIATED_BY_SYSTEM = "SYSTEM";
    public static final String INITIATED_BY_USER = "REST_USER";

    private final ProcessExecutionLogService processExecutionLogService;;

    private final DateService dateService;

    public ProcessController(ProcessExecutionLogService processExecutionLogService, DateService dateService) {
        this.processExecutionLogService = processExecutionLogService;
        this.dateService = dateService;
    }

    /**
     * Flow 1: Successful Automatic Initiation
     * Runs daily at 00:01 AM (Cron expression: "0 1 0 * * *)
     */
    @Scheduled(cron = "0 * * * * *")
    public void scheduledXmlProcessing() {
        log.info("Triggering Flow 1: Scheduled Automatic Initiation at 00:01 AM");
        try
        {
            ProcessExecutionLogDTO result = processExecutionLogService.executeProcess(InitiationType.SCHEDULED,
                    getBusinessDate(LocalDate.now()));
            log.info("Scheduled execution finished with status: {}", result.getStatus());
        } catch (Exception e) {
            log.error("The provided date is not a valid business day: {}", e.getMessage());
        }
    }

    /**
     * Manual Trigger REST Endpoint (Flow 3)
     */
    @PostMapping("/trigger")
    public ResponseEntity<ProcessExecutionLogDTO> triggerManualProcessing(@RequestParam(required = false) LocalDate businessDate) throws NonBusinessDayException {
        ProcessExecutionLogDTO result = processExecutionLogService.executeProcess(InitiationType.MANUAL, getBusinessDate(businessDate));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<ProcessExecutionLogDTO>> getListOfFailedProcess() {
        List<ProcessExecutionLogDTO> result = processExecutionLogService.findAllFailedProcess();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/latest")
    public ResponseEntity<ProcessExecutionLogDTO> getLatestProcessExecution(
            @RequestParam(required = false) LocalDate businessDate) throws NonBusinessDayException {

        LocalDate targetDate = getBusinessDate(businessDate);

        return processExecutionLogService.findLatestByBusinessDate(targetDate)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private @NonNull LocalDate getBusinessDate(LocalDate businessDate) throws NonBusinessDayException {
        if(businessDate == null) {
            businessDate = LocalDate.now();
        }
        if (!dateService.isBusinessDay(businessDate)) {
            throw new NonBusinessDayException(""+businessDate);
        }
        return businessDate;
    }
}
