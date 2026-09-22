package com.example.paymentservice.request;

public record PaymentCreateRequest(
    String name,
    String number,
    String email,
    String address,
    int billValue,
    String cardNumber,
    String cardHolder,
    String dateValue,
    String cvc
) {
}
