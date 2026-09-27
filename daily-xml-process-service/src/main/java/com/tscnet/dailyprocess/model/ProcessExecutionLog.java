package com.tscnet.dailyprocess.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity @Table(name="process_execution", uniqueConstraints=@UniqueConstraint(name="uk_process_business_date", columnNames="business_date"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProcessExecutionLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="business_date", nullable=false) private LocalDate businessDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private InitiationType initiationType;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private ExecutionStatus status;
    @Column(nullable=false) private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String triggeredBy;
    private String failureReason;
    @Column(nullable=false) private int totalFilesCount;
    @Column(nullable=false) private int processedFilesCount;
    @Column(nullable=false) private int failedFilesCount;
    @OneToMany(mappedBy="execution", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default private List<ProcessFileLog> fileLogs = new ArrayList<>();
    public void addFileLog(ProcessFileLog file) { file.setExecution(this); fileLogs.add(file); }
}
