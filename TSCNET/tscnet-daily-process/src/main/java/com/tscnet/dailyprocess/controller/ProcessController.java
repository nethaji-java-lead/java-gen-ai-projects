package com.tscnet.dailyprocess.controller;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.model.InitiationType;
import com.tscnet.dailyprocess.service.DateService;
import com.tscnet.dailyprocess.service.SftpXmlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/process")
@Slf4j
public class ProcessController {

    public static final String INITIATED_BY_SYSTEM = "SYSTEM";
    public static final String INITIATED_BY_USER = "REST_USER";

    private final SftpXmlService sftpXmlService;;

    private final DateService dateService;

    public ProcessController(SftpXmlService sftpXmlService, DateService dateService) {
        this.sftpXmlService = sftpXmlService;
        this.dateService = dateService;
    }

    /**
     * Flow 1: Successful Automatic Initiation
     * Runs daily at 00:01 AM (Cron expression: "0 1 0 * * *")
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void scheduledXmlProcessing() {
        log.info("Triggering Flow 1: Scheduled Automatic Initiation at 00:01 AM");
        try {
            ProcessExecutionLogDTO result = sftpXmlService.executeProcess(
                    InitiationType.SCHEDULED, LocalDate.now()
            );
            log.info("Scheduled execution finished with status: {}", result.getStatus());
        } catch (Exception e) {
            log.error("Error running scheduled XML processing", e);
        }
    }

    /**
     * Manual Trigger REST Endpoint (Flow 3)
     */
    @PostMapping("/trigger")
    public ResponseEntity<ProcessExecutionLogDTO> triggerManualProcessing(@RequestParam(required = false) LocalDate businessDate) {
        if(businessDate==null) {
            businessDate = LocalDate.now();
        }
        if (!dateService.isBusinessDay(businessDate)) {
            log.info("Invalid business day: {}", businessDate);
            throw new IllegalArgumentException(
                    "The provided date is not a valid business day: " + businessDate
            );
        }
        ProcessExecutionLogDTO result = sftpXmlService.executeProcess(InitiationType.MANUAL, businessDate);
        return ResponseEntity.ok(result);
    }
}
