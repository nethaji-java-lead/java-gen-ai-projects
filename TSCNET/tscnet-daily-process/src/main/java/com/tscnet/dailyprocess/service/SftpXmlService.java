package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.dto.ProcessFileLogDTO;
import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import com.tscnet.dailyprocess.model.*;
import com.tscnet.dailyprocess.model.xmlelement.BalancingMarketDocument;
import com.tscnet.dailyprocess.repository.ProcessExecutionLogRepository;
import com.tscnet.dailyprocess.repository.ProcurementAssessmentRepository;
import com.tscnet.dailyprocess.repository.ProcurementOfferRepository;
import com.tscnet.dailyprocess.validator.XmlValidationResult;
import com.tscnet.dailyprocess.validator.XmlValidator;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SftpXmlService {

    private static final String FAILED = "FAILED";
    private static final String SUCCESS = "SUCCESS";
    private final SftpRemoteFileTemplate sftpRemoteFileTemplate;
    private final XmlValidator xmlValidator;
    private final ProcessExecutionLogRepository executionLogRepository;
    private final ProcurementAssessmentService procurementAssessmentService;
    private final ProcurementDataPersistenceService procurementDataPersistenceService;
    private final ProcurementAssessmentRepository procurementAssessmentRepository;
    private final ProcurementOfferRepository procurementOfferRepository;

    private static final String DIR_FILES_TO_PROCESS = "filesToProcess";
    private static final String DIR_ARCHIVE = "archive";
    private static final String DIR_ERROR = "error";

    public ProcessExecutionLogDTO executeProcess(InitiationType initiationType, LocalDate businessDate) {

        var existing = executionLogRepository.findByBusinessDate(businessDate);
        if (existing.isPresent() && existing.get().getStatus() == ExecutionStatus.SUCCESS) {
            return mapToProcessExecutionLogDTO(existing.get());
        }


        String initiatedBy = (initiationType == InitiationType.MANUAL) ? "User" : "System";
        log.info("Starting SFTP XML file processing. InitiateType: {}, InitiatedBy: {}", initiationType, initiatedBy);

        // 1. Create Initial Execution Audit Log
        ProcessExecutionLog executionLog = ProcessExecutionLog.builder().initiationType(initiationType).status(ExecutionStatus.IN_PROGRESS).startTime(LocalDateTime.now()).businessDate(businessDate).initiatedBy(initiatedBy).build();

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

                            // 1. Parse XML into domain objects
                            BalancingMarketDocument balancingMarketDocument = parseXml(xml);

                            // 2. Extract procurement offers
                            List<ProcurementOfferDTO> procurementOffers = balancingMarketDocument.extractProcurementOffer();

                            System.out.println(procurementOffers);

                            // 3. Assess procurement offers
                            ProcurementAssessmentDTO assessment = procurementAssessmentService.procurementAssessment(procurementOffers);

                            // 4. Decide what to do with the file
                            if (assessment.status() == AssessmentStatus.ACCEPT) {
                                procurementDataPersistenceService.persistProcessedData(balancingMarketDocument, procurementOffers, assessment, fileName);
                                // Archive file on SFTP server
                                //session.rename(remoteFile, archiveFile);
                                log.info("File successfully moved to archive: {}", archiveFile);

                                recordFileLog(currentLog, fileName, SUCCESS, archiveFile, null);

                            } else {

                                log.warn("Procurement assessment did not pass for {}. Status={}, reason={}", fileName, assessment.status(), assessment.reason());

                                session.rename(remoteFile, errorFile);

                                recordFileLog(currentLog, fileName, FAILED, errorFile, assessment.reason());

                            }
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

    private ProcessExecutionLogDTO mapToProcessExecutionLogDTO(ProcessExecutionLog entity) {
        return ProcessExecutionLogDTO.builder().id(entity.getId()).initiationType(entity.getInitiationType()).status(entity.getStatus()).startTime(entity.getStartTime()).endTime(entity.getEndTime()).totalFilesCount(entity.getTotalFilesCount()).processedFilesCount(entity.getProcessedFilesCount()).failedFilesCount(entity.getFailedFilesCount()).triggeredBy(entity.getInitiatedBy()).fileLogs(entity.getFileLogs().stream().map(this::mapToProcessFileLogDTO).toList()).build();
    }

    private ProcessFileLogDTO mapToProcessFileLogDTO(ProcessFileLog entity) {

        return ProcessFileLogDTO.builder().id(entity.getId()).fileName(entity.getFileName()).status(entity.getStatus()).errorMessage(entity.getErrorMessage()).build();
    }

    private void recordFileLog(ProcessExecutionLog executionLog, String fileName, String status, String destPath, String error) {
        ProcessFileLog fileLog = ProcessFileLog.builder().fileName(fileName).status(status).destinationPath(destPath).errorMessage(error).processedAt(LocalDateTime.now()).build();

        executionLog.addFileLog(fileLog);
    }

    private void moveToArchive(String remoteFile, String archiveFile) {
        log.info("Archiving file: {}", remoteFile);
        sftpRemoteFileTemplate.rename(remoteFile, archiveFile);
        log.info("XML file successfully archived: {}", archiveFile);
    }

    private BalancingMarketDocument parseXml(String xml) {
        try {
            JAXBContext context = JAXBContext.newInstance(BalancingMarketDocument.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();

            return (BalancingMarketDocument) unmarshaller.unmarshal(new StringReader(xml));

        } catch (JAXBException e) {
            throw new RuntimeException("Failed to parse XML", e);
        }
    }

    @Transactional
    protected void persistProcessedData(BalancingMarketDocument document,
                                        List<ProcurementOfferDTO> offers,
                                        ProcurementAssessmentDTO assessment,
                                        String fileName) {
        log.info("Persisting assessment and offers for file: {}", fileName);

        // 1. Map ProcurementAssessment record/DTO to Entity
        ProcurementAssessment assessmentEntity = assessment.toProcurementOfferEntity(fileName);

        procurementAssessmentRepository.save(assessmentEntity);

        // 2. Map List<ProcurementOffer> records/DTOs to List<ProcurementOfferEntity>
        List<ProcurementOffer> offerEntities = offers.stream()
                .map(offer -> offer.toProcurementOfferEntity(fileName)).toList();

        procurementOfferRepository.saveAll(offerEntities);
    }
}