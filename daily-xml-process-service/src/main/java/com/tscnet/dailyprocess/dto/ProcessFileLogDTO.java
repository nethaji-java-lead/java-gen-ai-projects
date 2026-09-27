package com.tscnet.dailyprocess.dto;
import com.tscnet.dailyprocess.model.FileStatus;
import java.time.LocalDateTime;
public record ProcessFileLogDTO(Long id,String fileName,FileStatus status,String destinationPath,String errorMessage,LocalDateTime processedAt,String documentMrid) {}
