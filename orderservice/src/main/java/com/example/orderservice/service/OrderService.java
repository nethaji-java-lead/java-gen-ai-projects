package com.example.orderservice.service;

import com.example.orderservice.model.Order;

public interface OrderService {

    Order processOrder(Order order);

    boolean isOrderProcessed(Long paymentId);
}
