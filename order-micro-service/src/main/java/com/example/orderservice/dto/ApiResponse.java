package com.example.orderservice.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {

    // 3-parameter constructor
    public ApiResponse(boolean success,
            String message
    ) {
        this(success, message, null, LocalDateTime.now());
    }

    // 4-parameter constructor
    public ApiResponse(
            boolean success,
            String message,
            T data
    ) {
        this(success, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true,
                message,
                data
        );
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(
                false,
                message
        );
    }
}
