package com.example.paymentservice.service;

import com.example.paymentservice.event.PaymentCreatedEvent;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.producer.PaymentEventProducer;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.request.PaymentCreateRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService
{

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Override
    public Payment createPayment(PaymentCreateRequest paymentCreateRequest) {
        Payment payment = convertToEntity(paymentCreateRequest);
        payment =  paymentRepository.save(payment);

        // Publish the payment created event
//        PaymentCreatedEvent paymentCreatedEvent = new PaymentCreatedEvent(UUID.randomUUID().toString(),
//                payment.getId(),
//                payment.getName(), payment.getEmail(),
//                payment.getAddress(), payment.getBillValue());
//        paymentEventProducer.publishPaymentCreatedEvent(paymentCreatedEvent);
        return payment;
    }

    @Override
    public Optional<Payment> fetchPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public List<Payment> fetchAllPayments() {
        return paymentRepository.findAll();
    }

    @Cacheable(value = "payments", key = "#id")
    public Payment getPaymentById(Integer id) {
        System.out.println("Fetching payment from database...");
        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));
    }

    @CachePut(value = "payments", key = "#id")
    public Payment updatePayment(Integer id, PaymentCreateRequest request) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        payment.setEmail("test@gmail.com");
        // Update other fields

        return paymentRepository.save(payment);
    }

    private Payment convertToEntity(PaymentCreateRequest paymentCreateRequest) {
        Payment payment = new Payment();
        BeanUtils.copyProperties(paymentCreateRequest, payment);
        return payment;
    }
}
