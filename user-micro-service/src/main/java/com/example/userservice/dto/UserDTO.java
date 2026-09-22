package com.example.userservice.dto;

public record UserDTO(
        Long customerId,
        String name,
        String address,
        String shippingAddress,
        String email,
        String phone,
        boolean emailNotifications,
        boolean smsNotifications
) {
}
