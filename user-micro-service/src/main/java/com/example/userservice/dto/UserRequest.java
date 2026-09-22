package com.example.userservice.dto;

public record UserRequest(
        String name,
        String address,
        String shippingAddress,
        String email,
        String phone,
        boolean emailNotifications,
        boolean smsNotifications
) {
}
