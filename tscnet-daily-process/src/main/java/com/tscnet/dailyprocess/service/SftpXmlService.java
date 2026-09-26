package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.dto.ProcessFileLogDTO;
import com.tscnet.dailyprocess.model.ExecutionStatus;
import com.tscnet.dailyprocess.model.InitiationType;
import com.tscnet.dailyprocess.model.ProcessExecutionLog;
import com.tscnet.dailyprocess.model.ProcessFileLog;
import com.tscnet.dailyprocess.repository.ProcessExecutionLogRepository;
import com.tscnet.dailyprocess.validator.XmlValidationResult;
import com.tscnet.dailyprocess.validator.XmlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SftpXmlService {

    private static final String FAILED = "FAILED";
    private static final String SUCCESS = "SUCCESS";
    private final SftpRemoteFileTemplate sftpRemoteFileTemplate;
    private final XmlValidator xmlValidator;
    private final ProcessExecutionLogRepository executionLogRepository;

    private static final String DIR_FILES_TO_PROCESS = "filesToProcess";
    private static final String DIR_ARCHIVE = "archive";
    private static final String DIR_ERROR = "error";

    public ProcessExecutionLogDTO executeProcess(InitiationType initiationType, String initiatedBy) {
        log.info("Starting SFTP XML file processing. InitiateType: {}, InitiatedBy: {}", initiationType, initiatedBy);

        // 1. Create Initial Execution Audit Log
        ProcessExecutionLog executionLog = ProcessExecutionLog.builder()
                .initiationType(initiationType)
                .status(ExecutionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .triggeredBy(initiatedBy)
                .build();

        executionLog = executionLogRepository.save(executionLog);

        int totalCount = 0;
        int processedCount = 0;
        int failedCount = 0;

        try {
            ProcessExecutionLog currentLog = executionLog;

            sftpRemoteFileTemplate.execute(session -> {
                SftpClient.DirEntry[] files = session.list(DIR_FILES_TO_PROCESS);

                for (SftpClient.DirEntry file : files) {
                    String fileName = file.getFilename();

                    if (!fileName.toLowerCase().endsWith(".xml")) {
                        continue;
                    }

                    String remoteFile = DIR_FILES_TO_PROCESS + "/" + fileName;
                    String archiveFile = DIR_ARCHIVE + "/" + fileName;
                    String errorFile = DIR_ERROR + "/" + fileName;

                    log.info("Processing file: {}", remoteFile);

                    String xml;
                    // Read file content and close input stream before moving file
                    try (InputStream inputStream = session.readRaw(remoteFile)) {
                        xml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        log.error("Failed to read raw stream for file: {}", fileName, e);
                        recordFileLog(currentLog, fileName, FAILED, errorFile, e.getMessage());
                        session.rename(remoteFile, errorFile);
                        continue;
                    }

                    // Validate XML against XSD
                    XmlValidationResult validationResult = xmlValidator.validateXml(xml);

                    if (validationResult.valid()) {
                        try {
                            // TODO: Add database persistence logic for valid XML payload here

                            // Move valid file to archive
                            //session.rename(remoteFile, archiveFile);
                            log.info("XML file successfully archived: {}", archiveFile);

                            recordFileLog(currentLog, fileName, SUCCESS, archiveFile, null);
                        } catch (Exception e) {
                            log.error("Failed to process valid XML content for file: {}", fileName, e);
                            session.rename(remoteFile, errorFile);
                            recordFileLog(currentLog, fileName, FAILED, errorFile, e.getMessage());
                        }
                    } else {
                        log.warn("XML validation failed for file {}: {}", fileName, validationResult.errors());
                        session.rename(remoteFile, errorFile);
                        recordFileLog(currentLog, fileName, FAILED, errorFile, validationResult.errors().toString());
                    }
                }
                return null;
            });

            // Count metrics from file logs
            totalCount = currentLog.getFileLogs().size();
            processedCount = (int) currentLog.getFileLogs().stream().filter(f -> "SUCCESS".equals(f.getStatus())).count();
            failedCount = (int) currentLog.getFileLogs().stream().filter(f -> "FAILED".equals(f.getStatus())).count();

            // Set final status
            if (failedCount == 0) {
                executionLog.setStatus(ExecutionStatus.SUCCESS);
            } else if (processedCount > 0) {
                executionLog.setStatus(ExecutionStatus.PARTIAL_SUCCESS);
            } else {
                executionLog.setStatus(ExecutionStatus.FAILED);
            }

        } catch (Exception e) {
            log.error("Critical error during SFTP execution: {}", e.getMessage());
            executionLog.setStatus(ExecutionStatus.FAILED);
        } finally {
            executionLog.setEndTime(LocalDateTime.now());
            executionLog.setTotalFilesCount(totalCount);
            executionLog.setProcessedFilesCount(processedCount);
            executionLog.setFailedFilesCount(failedCount);

            executionLog = executionLogRepository.save(executionLog);
        }

        return mapToProcessExecutionLogDTO(executionLog);
    }

    private ProcessExecutionLogDTO mapToProcessExecutionLogDTO(ProcessExecutionLog entity)
    {
        return ProcessExecutionLogDTO.builder()
                .id(entity.getId())
                .initiationType(entity.getInitiationType())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .totalFilesCount(entity.getTotalFilesCount())
                .processedFilesCount(entity.getProcessedFilesCount())
                .failedFilesCount(entity.getFailedFilesCount())
                .triggeredBy(entity.getTriggeredBy())
                .fileLogs(
                        entity.getFileLogs()
                                .stream()
                                .map(this::mapToProcessFileLogDTO)
                                .toList()
                )
                .build();
    }

    private ProcessFileLogDTO mapToProcessFileLogDTO(ProcessFileLog entity) {

        return ProcessFileLogDTO.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    private void recordFileLog(ProcessExecutionLog executionLog, String fileName, String status, String destPath, String error) {
        ProcessFileLog fileLog = ProcessFileLog.builder()
                .fileName(fileName)
                .status(status)
                .destinationPath(destPath)
                .errorMessage(error)
                .processedAt(LocalDateTime.now())
                .build();

        executionLog.addFileLog(fileLog);
    }

    public boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SUNDAY;
    }
}