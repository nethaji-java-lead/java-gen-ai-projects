package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.model.*;
import com.tscnet.dailyprocess.repository.ProcessExecutionLogRepository;
import com.tscnet.dailyprocess.sftp.SftpFileClient;
import com.tscnet.dailyprocess.validator.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import java.io.*; import java.time.*; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class ProcessServiceTest {
  ProcessExecutionLogRepository repo=Mockito.mock(ProcessExecutionLogRepository.class);
  SftpFileClient sftp=Mockito.mock(SftpFileClient.class);
  NotificationService notifications=Mockito.mock(NotificationService.class);
  ProcessService service;
  @BeforeEach void setUp(){service=new ProcessService(repo,sftp,new XmlValidator(),new BusinessDayService(),notifications);}
  @Test void successfulFileIsArchived() throws Exception {
    LocalDate d=LocalDate.of(2026,9,25);
    var e=ProcessExecutionLog.builder().id(1L).businessDate(d).status(ExecutionStatus.IN_PROGRESS).fileLogs(new ArrayList<>()).build();
    Mockito.when(repo.findByBusinessDate(d)).thenReturn(Optional.empty());
    Mockito.when(repo.saveAndFlush(any())).thenReturn(e); Mockito.when(repo.save(any())).thenReturn(e);
    Mockito.when(sftp.listXmlFiles()).thenReturn(List.of("good.xml"));
    String xml="<root><mRID>DOC-1</mRID></root>";
    Mockito.when(sftp.read("filesToProcess/good.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));
    var r=service.execute(InitiationType.SCHEDULED,"SYSTEM",d);
    assertEquals(ExecutionStatus.SUCCESS,r.status()); verify(sftp).move("filesToProcess/good.xml","archive/good.xml");
  }
  @Test void invalidFileMovesToErrorAndFails() throws Exception {
    LocalDate d=LocalDate.of(2026,9,25);
    var e=ProcessExecutionLog.builder().id(2L).businessDate(d).status(ExecutionStatus.IN_PROGRESS).fileLogs(new ArrayList<>()).build();
    Mockito.when(repo.findByBusinessDate(d)).thenReturn(Optional.empty()); Mockito.when(repo.saveAndFlush(any())).thenReturn(e); Mockito.when(repo.save(any())).thenReturn(e);
    Mockito.when(sftp.listXmlFiles()).thenReturn(List.of("bad.xml")); Mockito.when(sftp.read(any())).thenReturn(new ByteArrayInputStream("<broken>".getBytes()));
    var r=service.execute(InitiationType.SCHEDULED,"SYSTEM",d);
    assertEquals(ExecutionStatus.FAILED,r.status()); verify(sftp).move("filesToProcess/bad.xml","error/bad.xml"); verify(notifications).notifyProcessFailure(eq(2L),anyString());
  }
}
