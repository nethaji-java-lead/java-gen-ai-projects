package com.example.orderservice.model;

import com.example.orderservice.enums.DlqStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "dlq_events",
        indexes = {
                @Index(
                        name = "idx_dlq_event_id",
                        columnList = "event_id",
                        unique = true
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DlqEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true
    )
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DlqStatus status;

}