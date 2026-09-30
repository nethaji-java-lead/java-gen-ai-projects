package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.dto.ProcessFileLogDTO;
import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import com.tscnet.dailyprocess.event.OperatorAlertEvent;
import com.tscnet.dailyprocess.event.ProcessExecutionNotificationEvent;
import com.tscnet.dailyprocess.exception.NoXmlFileException;
import com.tscnet.dailyprocess.model.*;
import com.tscnet.dailyprocess.model.xmlelement.BalancingMarketDocument;
import com.tscnet.dailyprocess.repository.ProcessExecutionLogRepository;
import com.tscnet.dailyprocess.validator.XmlValidationResult;
import com.tscnet.dailyprocess.validator.XmlValidator;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.tscnet.dailyprocess.service.NotificationService.TOPIC_OPERATOR_ALERTS;
import static com.tscnet.dailyprocess.service.NotificationService.TOPIC_PROCUREMENT_NOTIFICATIONS;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessExecutionLogService {

    private static final String FAILED = "FAILED";
    private static final String SUCCESS = "SUCCESS";

    private final SftpRemoteFileTemplate sftpRemoteFileTemplate;
    private final XmlValidator xmlValidator;
    private final ProcessExecutionLogRepository executionLogRepository;
    private final ProcurementAssessmentService procurementAssessmentService;
    private final ProcurementDataPersistenceService procurementDataPersistenceService;
    private final NotificationService notificationService;
    private final DateService dateService;

    private static final String DIR_FILES_TO_PROCESS = "filesToProcess";
    private static final String DIR_ARCHIVE = "archive";
    private static final String DIR_ERROR = "error";

    public ProcessExecutionLogDTO executeProcess(InitiationType initiationType, LocalDate businessDate) {

        if (!dateService.isBusinessDay(businessDate)) {
            log.info("Skipping SFTP XML processing for non-business day: {}", businessDate);
            return null;
        }

        String initiatedBy = (initiationType == InitiationType.MANUAL) ? "User" : "System";
        log.info("Starting SFTP XML file processing. InitiateType: {}, InitiatedBy: {}", initiationType, initiatedBy);

        ProcessExecutionLog executionLog = ProcessExecutionLog.builder()
                .initiationType(initiationType)
                .status(ExecutionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .businessDate(businessDate)
                .initiatedBy(initiatedBy)
                .build();

        executionLog = executionLogRepository.save(executionLog);
        ProcessExecutionLog currentLog = executionLog;

        try {
            sftpRemoteFileTemplate.execute(session -> {
                SftpClient.DirEntry[] files = session.list(DIR_FILES_TO_PROCESS);
                log.info("Retrieved Files: {}", files.length);

                // 1. Filter XML files
                List<SftpClient.DirEntry> xmlFiles = Arrays.stream(files)
                        .filter(file -> file.getFilename().toLowerCase().endsWith(".xml"))
                        .toList();

                // 2. Check if no XML files exist and throw exception
                if (xmlFiles.isEmpty()) {
                    throw new NoXmlFileException("No XML files found in SFTP location for processing");
                }

                // 3. Process XML files
                for (SftpClient.DirEntry file : xmlFiles) {
                    String fileName = file.getFilename();
                    String remoteFile = DIR_FILES_TO_PROCESS + "/" + fileName;
                    String archiveFile = DIR_ARCHIVE + "/" + fileName;
                    String errorFile = DIR_ERROR + "/" + fileName;

                    log.info("Processing file: {}", remoteFile);

                    InputStream inputStream = session.readRaw(remoteFile);
                    String xml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                    XmlValidationResult validationResult = xmlValidator.validateXml(xml);

                    if (validationResult.valid()) {
                        BalancingMarketDocument balancingMarketDocument = parseXml(xml);
                        List<ProcurementOfferDTO> procurementOffers = balancingMarketDocument.extractProcurementOffer();
                        ProcurementAssessmentDTO assessment = procurementAssessmentService.procurementAssessment(procurementOffers, businessDate);

                        if (assessment.status() == AssessmentStatus.ACCEPT) {
                            procurementDataPersistenceService.persistProcessedData(procurementOffers, assessment, fileName);
                            safelyMoveFileViaStream(session, remoteFile, archiveFile, xml.getBytes(StandardCharsets.UTF_8));
                            log.info("File successfully moved to archive: {}", archiveFile);
                            recordFileLog(currentLog, fileName, SUCCESS, archiveFile, null);
                        } else {
                            log.warn("Procurement assessment did not pass for {}. Status={}, reason={}", fileName, assessment.status(), assessment.reason());
                            safelyMoveFileViaStream(session, remoteFile, errorFile, xml.getBytes(StandardCharsets.UTF_8));
                            recordFileLog(currentLog, fileName, FAILED, errorFile, assessment.reason());
                        }
                    } else {
                        log.warn("XML validation failed for file {}: {}", fileName, validationResult.errors());
                        safelyMoveFileViaStream(session, remoteFile, errorFile, xml.getBytes(StandardCharsets.UTF_8));
                        recordFileLog(currentLog, fileName, FAILED, errorFile, validationResult.errors().toString());
                    }
                }
                return null;
            });
        } catch (Exception ex) {
            Throwable cause = ex;
            while (cause.getCause() != null && !(cause instanceof NoXmlFileException)) {
                cause = cause.getCause();
            }

            if (cause instanceof NoXmlFileException noXmlEx) {
                currentLog.setStatus(ExecutionStatus.NO_FILE_TO_PROCESS);
                currentLog.setTotalFilesCount(0);
                currentLog.setProcessedFilesCount(0);
                currentLog.setFailedFilesCount(0);
                currentLog.setEndTime(LocalDateTime.now());
                executionLogRepository.save(currentLog);
                throw noXmlEx;
            } else {
                String errorMessage = cause.getMessage() != null ? cause.getMessage() : ex.getMessage();

                // Notify operator via Kafka on exception/failure
                notifyOnDutyOperator(currentLog.getId(), businessDate, errorMessage);

                currentLog.setStatus(ExecutionStatus.FAILED);
                currentLog.setEndTime(LocalDateTime.now());
                executionLogRepository.save(currentLog);
            }
            throw ex;
        }

        // =========================================================================
        // CALCULATE FILE COUNTS AND UPDATE STATUS AFTER SFTP LOOP FINISHES
        // =========================================================================
        int totalFiles = currentLog.getFileLogs().size();
        long failedCount = currentLog.getFileLogs().stream()
                .filter(f -> FAILED.equals(f.getStatus()))
                .count();
        long successCount = totalFiles - failedCount;

        currentLog.setTotalFilesCount(totalFiles);
        currentLog.setProcessedFilesCount((int) successCount);
        currentLog.setFailedFilesCount((int) failedCount);
        currentLog.setEndTime(LocalDateTime.now());

        if (failedCount == 0) {
            currentLog.setStatus(ExecutionStatus.SUCCESS);
        } else if (successCount > 0) {
            currentLog.setStatus(ExecutionStatus.PARTIAL_SUCCESS);
        } else {
            currentLog.setStatus(ExecutionStatus.FAILED);
        }

        // Save updated log to database with non-zero file counts
        ProcessExecutionLog updatedLog = executionLogRepository.save(currentLog);

        // =========================================================================
        // SEND KAFKA NOTIFICATIONS BASED ON FINAL EXECUTION STATUS
        // =========================================================================
        switch (updatedLog.getStatus()) {
            case SUCCESS -> {
                notifySuccessExecution(updatedLog);
            }
            case PARTIAL_SUCCESS -> {
                notifySuccessExecution(updatedLog);
                notifyOnDutyOperator(
                        updatedLog.getId(),
                        businessDate,
                        String.format("Process execution completed with PARTIAL_SUCCESS. Processed: %d/%d, Failed: %d/%d",
                                successCount, totalFiles, failedCount, totalFiles)
                );
            }
            case FAILED -> {
                notifyOnDutyOperator(
                        updatedLog.getId(),
                        businessDate,
                        String.format("Process execution FAILED completely. Failed files: %d/%d",
                                failedCount, totalFiles)
                );
            }
            default -> log.warn("Unhandled execution status: {}", updatedLog.getStatus());
        }

        return mapToProcessExecutionLogDTO(updatedLog);
    }

    private void notifySuccessExecution(ProcessExecutionLog executionLog) {
        ProcessExecutionNotificationEvent event = new ProcessExecutionNotificationEvent(
                executionLog.getId(),
                executionLog.getBusinessDate(),
                executionLog.getInitiationType(),
                executionLog.getStatus(),
                executionLog.getTotalFilesCount(),
                executionLog.getProcessedFilesCount(),
                executionLog.getFailedFilesCount(),
                executionLog.getInitiatedBy(),
                Instant.now()
        );

        notificationService.sendNotificationEvent(
                TOPIC_PROCUREMENT_NOTIFICATIONS,
                String.valueOf(executionLog.getId()),
                event
        );
        log.info("Process execution notification [Status={}] published to Kafka for Execution ID: {}",
                executionLog.getStatus(), executionLog.getId());
    }

    private void notifyOnDutyOperator(Long executionId, LocalDate businessDate, String errorMessage) {
        OperatorAlertEvent alert = new OperatorAlertEvent(
                executionId,
                businessDate,
                errorMessage,
                Instant.now()
        );
        notificationService.sendNotificationEvent(TOPIC_OPERATOR_ALERTS, String.valueOf(executionId), alert);
        log.warn("Operator alert notification published to Kafka for Execution ID: {}", executionId);
    }

    public List<ProcessExecutionLogDTO> findAllFailedProcess() {
        List<ExecutionStatus> targetStatuses = List.of(ExecutionStatus.FAILED, ExecutionStatus.PARTIAL_SUCCESS);

        return executionLogRepository.findByStatusIn(targetStatuses)
                .stream()
                .map(this::mapToProcessExecutionLogWithFailedFilesOnly)
                .toList();
    }

    public Optional<ProcessExecutionLogDTO> findLatestByBusinessDate(LocalDate businessDate) {
        return executionLogRepository.findFirstByBusinessDateOrderByStartTimeDesc(businessDate)
                .map(this::mapToProcessExecutionLogDTO);
    }

    private ProcessExecutionLogDTO mapToProcessExecutionLogWithFailedFilesOnly(ProcessExecutionLog entity) {
        List<ProcessFileLogDTO> failedFileLogs = entity.getFileLogs().stream()
                .filter(fileLog -> FAILED.equals(fileLog.getStatus()))
                .map(this::mapToProcessFileLogDTO)
                .toList();

        return ProcessExecutionLogDTO.builder()
                .id(entity.getId())
                .initiationType(entity.getInitiationType())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .totalFilesCount(entity.getTotalFilesCount())
                .processedFilesCount(entity.getProcessedFilesCount())
                .failedFilesCount(entity.getFailedFilesCount())
                .triggeredBy(entity.getInitiatedBy())
                .fileLogs(failedFileLogs)
                .build();
    }

    private ProcessExecutionLogDTO mapToProcessExecutionLogDTO(ProcessExecutionLog entity) {
        return ProcessExecutionLogDTO.builder()
                .id(entity.getId())
                .initiationType(entity.getInitiationType())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .totalFilesCount(entity.getTotalFilesCount())
                .processedFilesCount(entity.getProcessedFilesCount())
                .failedFilesCount(entity.getFailedFilesCount())
                .triggeredBy(entity.getInitiatedBy())
                .fileLogs(entity.getFileLogs().stream().map(this::mapToProcessFileLogDTO).toList())
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

    @SneakyThrows
    private BalancingMarketDocument parseXml(String xml) {
        JAXBContext context = JAXBContext.newInstance(BalancingMarketDocument.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (BalancingMarketDocument) unmarshaller.unmarshal(new StringReader(xml));
    }

    private void safelyMoveFileViaStream(Session<?> session, String sourcePath, String destPath, byte[] content) throws IOException {
        if (session.exists(destPath)) {
            session.remove(destPath);
        }

        InputStream in = new ByteArrayInputStream(content);
        session.write(in, destPath);
        session.remove(sourcePath);
    }

    public List<ProcessExecutionLogDTO> findAllProcessLogs() {
        return executionLogRepository.findAll()
                .stream()
                .map(this::mapToProcessExecutionLogDTO)
                .toList();
    }
}