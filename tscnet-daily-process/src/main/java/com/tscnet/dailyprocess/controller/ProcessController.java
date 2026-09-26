package com.tscnet.dailyprocess.controller;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.model.InitiationType;
import com.tscnet.dailyprocess.model.ProcessExecution;
import com.tscnet.dailyprocess.model.ProcessExecutionLog;
import com.tscnet.dailyprocess.service.SftpXmlService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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

    private final SftpXmlService sftpXmlService;;

    public ProcessController(SftpXmlService sftpXmlService) {
        this.sftpXmlService = sftpXmlService;
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
                    InitiationType.SCHEDULED, INITIATED_BY_SYSTEM
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
    public ResponseEntity<ProcessExecutionLogDTO> triggerManualProcessing() {
        log.info("Manual execution requested via REST API");
        ProcessExecutionLogDTO result = sftpXmlService.executeProcess(InitiationType.MANUAL, INITIATED_BY_USER);
        return ResponseEntity.ok(result);
    }
}
