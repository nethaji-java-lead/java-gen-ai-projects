package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.exception.NoXmlFileException;
import com.tscnet.dailyprocess.model.InitiationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessExecutionScheduler {

    private final ProcessExecutionLogService processExecutionLogService;
    private final DateService dateService;

    @Value("${app.scheduler.enabled:false}")
    private boolean isSchedulerEnabled;

    // Adjust CRON pattern (e.g., runs at 00:01 AM) according to requirement
    @Scheduled(cron = "${app.scheduler.cron:0 1 0 * * *}")
    public void runDailyProcess() {
        if (isSchedulerEnabled) {
            LocalDate today = LocalDate.now();

            if (!dateService.isBusinessDay(today)) {
                log.info("Scheduled task skipped: {} is not a business day.", today);
                return;
            }

            log.info("Triggering automated daily process execution for business date: {}", today);
            try {
                processExecutionLogService.executeProcess(InitiationType.SCHEDULED, today);
            } catch (NoXmlFileException e) {
                log.info("{} for the Business Date: {}", e.getMessage(), today);
            }catch (Exception e) {
                log.error("Scheduled process execution failed for date: {}", today);
            }
        }
    }
}
