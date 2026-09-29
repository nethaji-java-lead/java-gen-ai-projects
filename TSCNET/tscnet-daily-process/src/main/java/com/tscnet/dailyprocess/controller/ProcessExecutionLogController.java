package com.tscnet.dailyprocess.controller;

import com.tscnet.dailyprocess.dto.ProcessExecutionLogDTO;
import com.tscnet.dailyprocess.dto.ProcurementAssessmentDTO;
import com.tscnet.dailyprocess.dto.ProcurementOfferDTO;
import com.tscnet.dailyprocess.exception.NonBusinessDayException;
import com.tscnet.dailyprocess.model.InitiationType;
import com.tscnet.dailyprocess.service.DateService;
import com.tscnet.dailyprocess.service.ProcessExecutionLogService;
import com.tscnet.dailyprocess.service.ProcurementAssessmentService;
import com.tscnet.dailyprocess.service.ProcurementOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/process")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Process Management", description = "APIs for managing and monitoring process executions, procurement assessments, and offers.")
public class ProcessExecutionLogController {

    private final ProcessExecutionLogService processExecutionLogService;
    private final DateService dateService;
    private final ProcurementAssessmentService procurementAssessmentService;
    private final ProcurementOfferService procurementOfferService;

    @Operation(
            summary = "Trigger manual process execution",
            description = "Triggers process execution for a specified business date after validating that the date is a valid business day."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Process triggered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProcessExecutionLogDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date provided or date is a non-business day",
                    content = @Content
            )
    })
    @PostMapping("/trigger")
    public ResponseEntity<ProcessExecutionLogDTO> triggerManualProcessing(
            @Parameter(description = "Business date to run processing for (yyyy-MM-dd)", required = true, example = "2026-03-30")
            @RequestParam @Valid @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) throws NonBusinessDayException {
        LocalDate targetDate = validateAndGetBusinessDate(businessDate);
        ProcessExecutionLogDTO result = processExecutionLogService.executeProcess(InitiationType.MANUAL, targetDate);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Fetch all execution logs",
            description = "Retrieves a complete list of all process execution logs."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved process logs",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProcessExecutionLogDTO.class)))
    )
    @GetMapping("/logs")
    public ResponseEntity<List<ProcessExecutionLogDTO>> getAllExecutionLogs() {
        return ResponseEntity.ok(processExecutionLogService.findAllProcessLogs());
    }

    @Operation(
            summary = "Fetch failed execution logs",
            description = "Retrieves a list of process execution logs that resulted in failure."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved failed process logs",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProcessExecutionLogDTO.class)))
    )
    @GetMapping("/failed")
    public ResponseEntity<List<ProcessExecutionLogDTO>> getListOfFailedProcess() {
        return ResponseEntity.ok(processExecutionLogService.findAllFailedProcess());
    }

    @Operation(
            summary = "Get latest process execution for a date",
            description = "Retrieves the most recent process execution record for a given business date."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Latest execution log found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProcessExecutionLogDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No process execution log found for the given date",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Date provided is a non-business day",
                    content = @Content
            )
    })
    @GetMapping("/latest")
    public ResponseEntity<ProcessExecutionLogDTO> getLatestProcessExecution(
            @Parameter(description = "Business date (yyyy-MM-dd)", example = "2026-03-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) throws NonBusinessDayException {
        LocalDate targetDate = validateAndGetBusinessDate(businessDate);
        return processExecutionLogService.findLatestByBusinessDate(targetDate)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get procurement assessments",
            description = "Retrieves procurement assessment records for the specified business date (defaults to today)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved procurement assessments",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProcurementAssessmentDTO.class)))
    )
    @GetMapping("/procurement")
    public ResponseEntity<List<ProcurementAssessmentDTO>> getProcurements(
            @Parameter(description = "Business date (yyyy-MM-dd). Defaults to current date if omitted.", example = "2026-03-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        LocalDate targetDate = (businessDate != null) ? businessDate : LocalDate.now();
        return ResponseEntity.ok(procurementAssessmentService.findByBusinessDate(targetDate));
    }

    @Operation(
            summary = "Get procurement offers",
            description = "Retrieves procurement offer records for the specified business date (defaults to today)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved procurement offers",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProcurementOfferDTO.class)))
    )
    @GetMapping("/offers")
    public ResponseEntity<List<ProcurementOfferDTO>> getOffers(
            @Parameter(description = "Business date (yyyy-MM-dd). Defaults to current date if omitted.", example = "2026-03-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        LocalDate targetDate = (businessDate != null) ? businessDate : LocalDate.now();
        return ResponseEntity.ok(procurementOfferService.findByBusinessDate(targetDate));
    }

    private LocalDate validateAndGetBusinessDate(LocalDate businessDate) throws NonBusinessDayException {
        if (businessDate == null || !dateService.isBusinessDay(businessDate)) {
            throw new NonBusinessDayException("Non-business day specified: " + businessDate);
        }
        return businessDate;
    }
}