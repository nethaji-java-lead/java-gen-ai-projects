package com.example.noificationservice.service;

import com.example.noificationservice.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendPaymentSuccessNotification(
            PaymentCompletedEvent event) {

        log.info(
                "Sending payment success notification. orderId={}, customerId={}",
                event.orderId(),
                event.customerId()
        );

        emailService.sendPaymentSuccessEmail(event.customerEmail()
                , event.orderNumber(), event.amount());
    }
}