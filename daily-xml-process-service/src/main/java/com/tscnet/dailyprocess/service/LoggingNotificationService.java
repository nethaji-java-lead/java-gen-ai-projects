package com.tscnet.dailyprocess.service;
import lombok.extern.slf4j.Slf4j; import org.springframework.stereotype.Component;
@Component @Slf4j public class LoggingNotificationService implements NotificationService {
 public void notifyProcessSuccess(Long id){log.info("NOTIFICATION process={} SUCCESS participants=business-ops",id);}
 public void notifyProcessFailure(Long id,String reason){log.error("NOTIFICATION process={} FAILED on-duty-operator reason={}",id,reason);}
}
