package com.tscnet.dailyprocess.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "process_execution_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitiationType initiationType; // SCHEDULED or MANUAL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status; // IN_PROGRESS, SUCCESS, FAILED, PARTIAL_SUCCESS

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int totalFilesCount;
    private int processedFilesCount;
    private int failedFilesCount;

    private String triggeredBy; // "SYSTEM" or Username

    @OneToMany(mappedBy = "executionLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<ProcessFileLog> fileLogs = new ArrayList<>();

    public void addFileLog(ProcessFileLog fileLog) {
        fileLogs.add(fileLog);
        fileLog.setExecutionLog(this);
    }
}