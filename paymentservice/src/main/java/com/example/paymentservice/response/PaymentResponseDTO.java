package com.example.paymentservice.response;

public record PaymentResponseDTO(String name,
                                 String number,
                                 String email,
                                 String address,
                                 int billValue,
                                 String cardNumber,
                                 String cardHolder,
                                 String dateValue,
                                 String cvc) {
}
