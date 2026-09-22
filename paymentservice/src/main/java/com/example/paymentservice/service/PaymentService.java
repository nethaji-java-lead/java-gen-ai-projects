package com.example.paymentservice.service;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.request.PaymentCreateRequest;

import java.util.List;
import java.util.Optional;

public interface PaymentService {

    Payment createPayment(PaymentCreateRequest paymentCreateRequest);

    Optional<Payment> fetchPaymentById(Integer paymentId);

    List<Payment> fetchAllPayments();

    Payment getPaymentById(Integer id);

    Payment updatePayment(Integer id, PaymentCreateRequest request);
}
