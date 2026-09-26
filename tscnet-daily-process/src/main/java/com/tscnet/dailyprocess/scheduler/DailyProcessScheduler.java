package com.tscnet.dailyprocess.scheduler;

import com.tscnet.dailyprocess.service.ProcessInitiationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class DailyProcessScheduler {

    @Value("${app.scheduler.enabled:true}")
    private boolean isSchedulerEnabled;

    private final ProcessInitiationService initiationService;

    public DailyProcessScheduler(
            ProcessInitiationService initiationService) {
        this.initiationService = initiationService;
    }

    @Scheduled(cron = "${app.scheduler.cron:0 1 0 * * *}")
    public void runDailyProcess() {
        if (!isSchedulerEnabled) {
            return;
        }
        LocalDate businessDate = LocalDate.now();
        initiationService.automaticInitiation(businessDate);
    }
}
