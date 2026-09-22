package com.example.orderservice.repository;

import com.example.orderservice.enums.EventStatus;
import com.example.orderservice.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findByStatus(EventStatus status);

    Optional<OrderEvent> findByOrderId(Long aggregateId);
}
