package com.tscnet.dailyprocess.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void notifyParticipants(String message) {
        log.info("PARTICIPANT NOTIFICATION: {}", message);
    }

    public void notifyOperator(String message) {
        log.warn("OPERATOR ALERT: {}", message);
    }
}
