package com.example.noificationservice.service;

import com.example.noificationservice.event.PaymentCompletedEvent;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    private final String fromNumber;

    public SmsService(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.from-number}") String fromNumber) {

        Twilio.init(accountSid, authToken);
        this.fromNumber = fromNumber;
    }

    public void sendPaymentSuccessSms(
            PaymentCompletedEvent event) {

        if (event.customerPhone() == null ||
                event.customerPhone().isBlank()) {

            log.warn(
                    "Customer phone number missing. orderId={}",
                    event.orderId()
            );
            return;
        }

        String message = String.format(
                "Payment successful! Order %s, amount %.2f. Thank you for your purchase.",
                event.orderNumber(),
                event.amount()
        );

        sendSms(event.customerPhone(), message);
    }

    private void sendSms(
            String phoneNumber,
            String message) {

        log.info(
                "Sending SMS. phone={}",
                maskPhone(phoneNumber)
        );

        Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromNumber),
                message
        ).create();

        log.info(
                "SMS sent successfully. phone={}",
                maskPhone(phoneNumber)
        );
    }

    public void recover(
            Exception exception,
            PaymentCompletedEvent event) {

        log.error(
                "SMS sending failed after all retries. orderId={}",
                event.orderId(),
                exception
        );

        // Production:
        // Save failed notification to DB
        // or publish to a Dead Letter Topic
    }

    private String maskPhone(String phoneNumber) {

        if (phoneNumber == null ||
                phoneNumber.length() < 4) {

            return "****";
        }

        return "****" +
                phoneNumber.substring(
                        phoneNumber.length() - 4
                );
    }
}