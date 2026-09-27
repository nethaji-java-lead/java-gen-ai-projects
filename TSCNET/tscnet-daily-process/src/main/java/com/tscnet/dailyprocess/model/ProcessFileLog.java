package com.tscnet.dailyprocess.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "process_file_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessFileLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id")
    @JsonManagedReference
    private ProcessExecutionLog executionLog;

    private String fileName;
    private String status; // SUCCESS, FAILED
    private String destinationPath; // /archive or /error

    @Column(length = 2000)
    private String errorMessage;

    private LocalDateTime processedAt;
}