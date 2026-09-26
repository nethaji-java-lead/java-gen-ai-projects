package com.tscnet.dailyprocess.controller;

import com.tscnet.dailyprocess.model.ProcessExecution;
import com.tscnet.dailyprocess.service.ProcessInitiationService;
import com.tscnet.dailyprocess.service.SftpXmlService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private final ProcessInitiationService service;

    private final SftpXmlService sftpXmlService;;

    public ProcessController(ProcessInitiationService service, SftpXmlService sftpXmlService) {
        this.service = service;
        this.sftpXmlService = sftpXmlService;
    }

    @PostMapping("/automatic")
    public ProcessExecution automatic(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        return service.automaticInitiation(businessDate);
    }

    @PostMapping("/manual")
    public ProcessExecution manual(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate businessDate,
            @RequestParam
            @NotBlank
            String operator) {

        return service.manualInitiation(businessDate, operator);
    }

    @GetMapping("/executions")
    public List<ProcessExecution> history(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        return businessDate == null ? service.history() : service.history(businessDate);
    }

    @PostMapping("/read")
    public List<String> readXmlFiles() {

        sftpXmlService.readXmlFile();

        return sftpXmlService.readXmlFiles();
    }
}
