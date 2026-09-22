package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order processOrder(Order order) {

        Long paymentId = order.getPaymentId();

        return orderRepository.findByPaymentId(paymentId)
                .orElseGet(() -> saveOrLoadExisting(order, paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderProcessed(Long paymentId) {

        return orderRepository.existsByPaymentId(paymentId);
    }

    private Order saveOrLoadExisting(Order order, Long paymentId) {

        try {
            return orderRepository.save(order);
        } catch (DataIntegrityViolationException ex) {
            // Handles concurrent duplicate events; DB unique key is source of truth.
            return orderRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> ex);
        }
    }
}