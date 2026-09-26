package com.tscnet.dailyprocess.dto;

import com.tscnet.dailyprocess.model.ExecutionStatus;
import com.tscnet.dailyprocess.model.InitiationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessExecutionLogDTO {

    private Long id;

    private InitiationType initiationType;

    private ExecutionStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int totalFilesCount;

    private int processedFilesCount;

    private int failedFilesCount;

    private String triggeredBy;

    @Builder.Default
    private List<ProcessFileLogDTO> fileLogs = new ArrayList<>();
}
