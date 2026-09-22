package com.example.paymentservice.repository;

import com.example.paymentservice.enums.EventStatus;
import com.example.paymentservice.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findByStatus(EventStatus status);

    Optional<OrderEvent> findByAggregateId(Long aggregateId);

}
