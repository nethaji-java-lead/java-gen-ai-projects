package com.example.orderservice.repository;

import com.example.orderservice.model.DlqEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DlqEventRepository
        extends JpaRepository<DlqEvent, Long> {

    Optional<DlqEvent> findByEventId(String eventId);
}