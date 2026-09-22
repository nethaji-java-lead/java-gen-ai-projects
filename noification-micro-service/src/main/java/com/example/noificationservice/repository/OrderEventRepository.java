package com.example.noificationservice.repository;

import com.example.noificationservice.enums.EventStatus;
import com.example.noificationservice.entity.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findByStatus(EventStatus status);

    Optional<OrderEvent> findByAggregateId(Long aggregateId);

}
