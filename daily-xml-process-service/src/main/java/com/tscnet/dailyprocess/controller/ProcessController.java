package com.tscnet.dailyprocess.controller;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.model.InitiationType;
import com.tscnet.dailyprocess.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessController {
    private final ProcessService service;

    @PostMapping("/trigger")
    public ResponseEntity<ProcessExecutionLogDTO> manual(@RequestParam(defaultValue = "") String businessDate) {
        LocalDate d = businessDate.isBlank() ? LocalDate.now() : LocalDate.parse(businessDate);
        return ResponseEntity.ok(service.execute(InitiationType.MANUAL, "REST_USER", d));
    }

    @GetMapping("/current")
    public ResponseEntity<ProcessExecutionLogDTO> current(@RequestParam(defaultValue = "") String businessDate) {
        LocalDate d = businessDate.isBlank() ? LocalDate.now() : LocalDate.parse(businessDate);
        var r = service.current(d);
        return r == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(r);
    }

    @GetMapping("/history")
    public List<ProcessExecutionLogDTO> history() {
        return service.history();
    }
}
