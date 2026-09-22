package com.example.noificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPaymentSuccessEmail(
            String to,
            String orderNumber,
            BigDecimal amount) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Payment Successful - Order " + orderNumber);

        message.setText("""
                Hi,

                Your payment was successfully completed.

                Order Number: %s
                Amount: %s
                Status: PAYMENT_COMPLETED

                Thank you for your purchase.

                Regards,
                Order Team
                """.formatted(orderNumber, amount));

        mailSender.send(message);

        log.info(
                "Payment success email sent. orderNumber={}, recipient={}",
                orderNumber,
                to
        );
    }
}