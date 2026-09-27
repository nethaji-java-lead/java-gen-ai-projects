package com.tscnet.dailyprocess.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="process_file_log", indexes=@Index(name="idx_file_name", columnList="file_name"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProcessFileLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="execution_id") private ProcessExecutionLog execution;
    @Column(name="file_name", nullable=false) private String fileName;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private FileStatus status;
    private String destinationPath;
    @Column(length=4000) private String errorMessage;
    private LocalDateTime processedAt;
    private String documentMrid;
}
