package com.tscnet.dailyprocess.dto;
import com.tscnet.dailyprocess.model.*;
import java.time.*; import java.util.*;
public record ProcessExecutionLogDTO(Long id,LocalDate businessDate,InitiationType initiationType,ExecutionStatus status,LocalDateTime startTime,LocalDateTime endTime,String triggeredBy,String failureReason,int totalFilesCount,int processedFilesCount,int failedFilesCount,List<ProcessFileLogDTO> fileLogs) {}
