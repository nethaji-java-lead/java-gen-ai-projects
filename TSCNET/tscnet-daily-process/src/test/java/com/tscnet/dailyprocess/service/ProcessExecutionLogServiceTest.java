package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.model.*;
import com.tscnet.dailyprocess.repository.ProcessExecutionLogRepository;
import com.tscnet.dailyprocess.validator.XmlValidationResult;
import com.tscnet.dailyprocess.validator.XmlValidator;
import org.apache.sshd.sftp.client.SftpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.file.remote.SessionCallback;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessExecutionLogServiceTest {

    @Mock
    private SftpRemoteFileTemplate sftpRemoteFileTemplate;
    @Mock
    private XmlValidator xmlValidator;
    @Mock
    private ProcessExecutionLogRepository executionLogRepository;
    @Mock
    private ProcurementAssessmentService procurementAssessmentService;
    @Mock
    private ProcurementDataPersistenceService procurementDataPersistenceService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private DateService dateService;
    @Mock
    private Session<SftpClient.DirEntry> session;

    @InjectMocks
    private ProcessExecutionLogService executionLogService;

    private final LocalDate businessDate = LocalDate.of(2026, 9, 28);

    @Test
    void executeProcess_ShouldReturnNull_WhenNotBusinessDay() {
        when(dateService.isBusinessDay(businessDate)).thenReturn(false);

        ProcessExecutionLogDTO result = executionLogService.executeProcess(InitiationType.SCHEDULED, businessDate);

        assertNull(result);
        verifyNoInteractions(sftpRemoteFileTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeProcess_ShouldProcessXmlFilesSuccessfully() throws Exception {
        when(dateService.isBusinessDay(businessDate)).thenReturn(true);

        ProcessExecutionLog initialLog = ProcessExecutionLog.builder()
                .id(1L)
                .fileLogs(new ArrayList<>())
                .businessDate(businessDate)
                .initiationType(InitiationType.MANUAL)
                .build();

        when(executionLogRepository.save(any())).thenReturn(initialLog);

        doAnswer(invocation -> {
            SessionCallback<SftpClient.DirEntry, Object> action = invocation.getArgument(0);

            SftpClient.DirEntry validXmlFile = mock(SftpClient.DirEntry.class);
            when(validXmlFile.getFilename()).thenReturn("data.xml");
            SftpClient.DirEntry nonXmlFile = mock(SftpClient.DirEntry.class);
            when(nonXmlFile.getFilename()).thenReturn("notes.txt");

            when(session.list("filesToProcess")).thenReturn(new SftpClient.DirEntry[]{validXmlFile, nonXmlFile});

            String xmlContent = "<Balancing_MarketDocument xmlns=\"urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4\">"
                    + "<mRID>MRID-100</mRID>"
                    + "</Balancing_MarketDocument>";
            InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
            when(session.readRaw("filesToProcess/data.xml")).thenReturn(inputStream);

            return action.doInSession(session);
        }).when(sftpRemoteFileTemplate).execute(any());

        XmlValidationResult validResult = new XmlValidationResult(true, List.of());
        when(xmlValidator.validateXml(anyString())).thenReturn(validResult);

        ProcurementAssessmentDTO assessmentDTO = new ProcurementAssessmentDTO(
                BigDecimal.TEN, BigDecimal.ONE, true, true, true, AssessmentStatus.ACCEPT, "Passed", businessDate
        );
        when(procurementAssessmentService.procurementAssessment(anyList(), eq(businessDate))).thenReturn(assessmentDTO);

        ProcessExecutionLogDTO result = executionLogService.executeProcess(InitiationType.MANUAL, businessDate);

        assertNotNull(result);
        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        verify(procurementDataPersistenceService, times(1)).persistProcessedData(anyList(), eq(assessmentDTO), eq("data.xml"));
        verify(notificationService, times(1)).sendNotificationEvent(eq(NotificationService.TOPIC_PROCUREMENT_NOTIFICATIONS), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeProcess_ShouldHandleInvalidXmlAndAssessmentFailures() throws Exception {
        when(dateService.isBusinessDay(businessDate)).thenReturn(true);

        ProcessExecutionLog initialLog = ProcessExecutionLog.builder()
                .id(1L)
                .fileLogs(new ArrayList<>())
                .businessDate(businessDate)
                .initiationType(InitiationType.SCHEDULED)
                .build();

        when(executionLogRepository.save(any())).thenReturn(initialLog);

        doAnswer(invocation -> {
            SessionCallback<SftpClient.DirEntry, Object> action = invocation.getArgument(0);

            SftpClient.DirEntry invalidXml = mock(SftpClient.DirEntry.class);
            when(invalidXml.getFilename()).thenReturn("invalid.xml");

            SftpClient.DirEntry rejectedXml = mock(SftpClient.DirEntry.class);
            when(rejectedXml.getFilename()).thenReturn("rejected.xml");

            when(session.list("filesToProcess")).thenReturn(new SftpClient.DirEntry[]{invalidXml, rejectedXml});

            String xmlContent = "<Balancing_MarketDocument xmlns=\"urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4\"></Balancing_MarketDocument>";
            when(session.readRaw(anyString()))
                    .thenReturn(new ByteArrayInputStream(xmlContent.getBytes()))
                    .thenReturn(new ByteArrayInputStream(xmlContent.getBytes()));

            return action.doInSession(session);
        }).when(sftpRemoteFileTemplate).execute(any());

        XmlValidationResult invalidResult = new XmlValidationResult(false, List.of("Invalid syntax"));
        XmlValidationResult validResult = new XmlValidationResult(true, List.of());

        when(xmlValidator.validateXml(anyString()))
                .thenReturn(invalidResult)
                .thenReturn(validResult);

        ProcurementAssessmentDTO rejectAssessment = new ProcurementAssessmentDTO(
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false, AssessmentStatus.REJECT, "Failed", businessDate
        );
        when(procurementAssessmentService.procurementAssessment(anyList(), eq(businessDate))).thenReturn(rejectAssessment);

        ProcessExecutionLogDTO result = executionLogService.executeProcess(InitiationType.SCHEDULED, businessDate);

        assertNotNull(result);
        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        verify(notificationService, times(1)).sendNotificationEvent(eq(NotificationService.TOPIC_OPERATOR_ALERTS), any(), any());
    }

    @Test
    void findAllFailedProcess_ShouldReturnOnlyFailedLogsAndFiles() {
        ProcessFileLog successFile = ProcessFileLog.builder().status("SUCCESS").fileName("a.xml").build();
        ProcessFileLog failedFile = ProcessFileLog.builder().status("FAILED").fileName("b.xml").build();

        ProcessExecutionLog logEntity = ProcessExecutionLog.builder()
                .id(10L)
                .status(ExecutionStatus.PARTIAL_SUCCESS)
                .fileLogs(List.of(successFile, failedFile))
                .build();

        when(executionLogRepository.findByStatusIn(anyList())).thenReturn(List.of(logEntity));

        List<ProcessExecutionLogDTO> results = executionLogService.findAllFailedProcess();

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getFileLogs().size());
        assertEquals("b.xml", results.get(0).getFileLogs().get(0).getFileName());
    }

    @Test
    void findLatestByBusinessDate_ShouldReturnMappedDTO_WhenFound() {
        ProcessExecutionLog logEntity = ProcessExecutionLog.builder()
                .id(5L)
                .businessDate(businessDate)
                .fileLogs(List.of())
                .build();

        when(executionLogRepository.findFirstByBusinessDateOrderByStartTimeDesc(businessDate))
                .thenReturn(Optional.of(logEntity));

        Optional<ProcessExecutionLogDTO> result = executionLogService.findLatestByBusinessDate(businessDate);

        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getId());
    }
}