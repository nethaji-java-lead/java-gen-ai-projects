package com.example.paymentservice.repository;

import com.example.paymentservice.enums.EventStatus;
import com.example.paymentservice.enums.OrderStatus;
import com.example.paymentservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query("""
    UPDATE Order e
    SET e.status = :status
    WHERE e.id =:orderId""")
    int updateStatus(
            @Param("orderId") Long orderId,
            @Param("status") OrderStatus status
    );
}
