package com.tscnet.dailyprocess.service;

import com.tscnet.dailyprocess.dto.*; import com.tscnet.dailyprocess.model.*; import com.tscnet.dailyprocess.repository.*; import com.tscnet.dailyprocess.sftp.*; import com.tscnet.dailyprocess.validator.*;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.io.*; import java.nio.charset.StandardCharsets; import java.time.*; import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class ProcessService {
 private static final String IN="filesToProcess/", ARCH="archive/", ERR="error/";
 private final ProcessExecutionLogRepository executionRepo; private final SftpFileClient sftp; private final XmlValidator validator; private final BusinessDayService businessDays; private final NotificationService notifications;
 @Transactional
 public ProcessExecutionLogDTO execute(InitiationType type,String by,LocalDate requestedDate){
   LocalDate bd=businessDays.applicableBusinessDate(requestedDate);
   if(type==InitiationType.SCHEDULED && !businessDays.isBusinessDay(requestedDate)) return null;
   var existing=executionRepo.findByBusinessDate(bd);
   if(existing.isPresent() && existing.get().getStatus()==ExecutionStatus.SUCCESS) return toDto(existing.get());
   ProcessExecutionLog logEntity;
   if(existing.isPresent()) { logEntity=existing.get(); logEntity.setInitiationType(type); logEntity.setTriggeredBy(by); logEntity.setStatus(ExecutionStatus.IN_PROGRESS); logEntity.setFailureReason(null); logEntity.setStartTime(LocalDateTime.now()); logEntity.setEndTime(null); logEntity.getFileLogs().clear(); }
   else { logEntity=ProcessExecutionLog.builder().businessDate(bd).initiationType(type).status(ExecutionStatus.IN_PROGRESS).startTime(LocalDateTime.now()).triggeredBy(by).build(); try{logEntity=executionRepo.saveAndFlush(logEntity);}catch(DataIntegrityViolationException e){return toDto(executionRepo.findByBusinessDate(bd).orElseThrow());} }
   try {
     List<String> files=sftp.listXmlFiles();
     for(String file:files) processFile(logEntity,file);
     long ok=logEntity.getFileLogs().stream().filter(f->f.getStatus()==FileStatus.SUCCESS).count(); long failed=logEntity.getFileLogs().stream().filter(f->f.getStatus()==FileStatus.FAILED).count();
     logEntity.setTotalFilesCount(logEntity.getFileLogs().size()); logEntity.setProcessedFilesCount((int)ok); logEntity.setFailedFilesCount((int)failed);
     logEntity.setStatus(failed==0?ExecutionStatus.SUCCESS:(ok>0?ExecutionStatus.PARTIAL_SUCCESS:ExecutionStatus.FAILED));
     if(logEntity.getStatus()==ExecutionStatus.SUCCESS||logEntity.getStatus()==ExecutionStatus.PARTIAL_SUCCESS) notifications.notifyProcessSuccess(logEntity.getId()); else notifications.notifyProcessFailure(logEntity.getId(),"All candidate files failed");
   } catch(Exception e){ log.error("Process initiation failed",e); logEntity.setStatus(ExecutionStatus.FAILED); logEntity.setFailureReason(e.getMessage()); notifications.notifyProcessFailure(logEntity.getId(),e.getMessage()); }
   logEntity.setEndTime(LocalDateTime.now()); return toDto(executionRepo.save(logEntity));
 }
 private void processFile(ProcessExecutionLog exec,String file){
   String source=IN+file; String destination;
   try(InputStream in=sftp.read(source)){
     byte[] bytes=in.readAllBytes(); XmlValidationResult result=validator.validate(new ByteArrayInputStream(bytes));
     if(!result.valid()){destination=ERR+file; safeMove(source,destination); exec.addFileLog(ProcessFileLog.builder().fileName(file).status(FileStatus.FAILED).destinationPath(destination).errorMessage(String.join("; ",result.errors())).processedAt(LocalDateTime.now()).documentMrid(result.documentMrid()).build()); return;}
     // Domain persistence/calculation hook: parse validated document and persist domain records here.
     destination=ARCH+file; safeMove(source,destination); exec.addFileLog(ProcessFileLog.builder().fileName(file).status(FileStatus.SUCCESS).destinationPath(destination).processedAt(LocalDateTime.now()).documentMrid(result.documentMrid()).build());
   }catch(Exception e){destination=ERR+file; try{safeMove(source,destination);}catch(Exception moveEx){e.addSuppressed(moveEx);} exec.addFileLog(ProcessFileLog.builder().fileName(file).status(FileStatus.FAILED).destinationPath(destination).errorMessage(e.getMessage()).processedAt(LocalDateTime.now()).build());}
 }
 private void safeMove(String s,String d)throws IOException{ sftp.move(s,d); }
 @Transactional(readOnly=true) public List<ProcessExecutionLogDTO> history(){return executionRepo.findTop50ByOrderByStartTimeDesc().stream().map(this::toDto).toList();}
 @Transactional(readOnly=true) public ProcessExecutionLogDTO current(LocalDate d){return executionRepo.findByBusinessDate(d).map(this::toDto).orElse(null);}
 private ProcessExecutionLogDTO toDto(ProcessExecutionLog e){return new ProcessExecutionLogDTO(e.getId(),e.getBusinessDate(),e.getInitiationType(),e.getStatus(),e.getStartTime(),e.getEndTime(),e.getTriggeredBy(),e.getFailureReason(),e.getTotalFilesCount(),e.getProcessedFilesCount(),e.getFailedFilesCount(),e.getFileLogs().stream().map(f->new ProcessFileLogDTO(f.getId(),f.getFileName(),f.getStatus(),f.getDestinationPath(),f.getErrorMessage(),f.getProcessedAt(),f.getDocumentMrid())).toList());}
}
