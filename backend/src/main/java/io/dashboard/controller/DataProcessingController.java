package io.dashboard.controller;

import io.dashboard.dto.*;
import io.dashboard.model.ProcessingStatus;
import io.dashboard.service.DataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class DataProcessingController {
    
    private final DataProcessingService dataProcessingService;
    
    @PostMapping("/uploads/{jobId}/process-data")
    public ResponseEntity<DataProcessingResponse> startDataProcessing(
            @PathVariable Long jobId,
            @RequestBody(required = false) DataProcessingRequest request) {
        
        try {
            if (request == null) {
                request = DataProcessingRequest.defaultRequest(jobId);
            } else {
                request.setUploadJobId(jobId);
            }
            
            CompletableFuture<DataProcessingResponse> future = dataProcessingService.processUploadJob(jobId);
            DataProcessingResponse response = future.get();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting data processing for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(DataProcessingResponse.builder()
                    .status("FAILED")
                    .message("Failed to start processing: " + e.getMessage())
                    .build());
        }
    }
    
    @GetMapping("/processing/{processingJobId}/status")
    public ResponseEntity<ProcessingStatusResponse> getProcessingStatus(@PathVariable Long processingJobId) {
        try {
            // This would typically query the database for the current status
            // For now, return a mock response
            ProcessingStatusResponse response = ProcessingStatusResponse.builder()
                    .jobId(processingJobId)
                    .status("RUNNING")
                    .progressPercentage(50.0)
                    .recordsProcessed(1000L)
                    .totalRecords(2000L)
                    .errorCount(5L)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting processing status for job {}: {}", processingJobId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/processing/{processingJobId}/errors")
    public ResponseEntity<List<Object>> getProcessingErrors(@PathVariable Long processingJobId) {
        try {
            // This would typically query the ProcessingErrorRepository
            // For now, return a mock response
            List<Object> errors = List.of(
                new HashMap<String, Object>() {{
                    put("rowNumber", 5);
                    put("errorMessage", "Invalid numeric value");
                    put("severity", "ERROR");
                }},
                new HashMap<String, Object>() {{
                    put("rowNumber", 12);
                    put("errorMessage", "Missing required field");
                    put("severity", "WARNING");
                }}
            );
            
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            log.error("Error getting processing errors for job {}: {}", processingJobId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/processing/{processingJobId}/retry")
    public ResponseEntity<DataProcessingResponse> retryFailedProcessing(@PathVariable Long processingJobId) {
        try {
            // This would typically restart the processing job
            DataProcessingResponse response = DataProcessingResponse.builder()
                    .processingJobId(processingJobId)
                    .status("PENDING")
                    .message("Processing job restarted")
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrying processing job {}: {}", processingJobId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(DataProcessingResponse.builder()
                    .status("FAILED")
                    .message("Failed to retry processing: " + e.getMessage())
                    .build());
        }
    }
    
    @GetMapping("/indicators/{indicatorId}/data")
    public ResponseEntity<List<FactValueResponse>> getIndicatorData(@PathVariable Long indicatorId) {
        try {
            // This would typically query the FactIndicatorValueRepository
            // For now, return a mock response
            List<FactValueResponse> data = List.of(
                FactValueResponse.builder()
                    .indicatorId(indicatorId)
                    .indicatorName("GDP")
                    .value(new java.math.BigDecimal("1000000"))
                    .timeValue("2023")
                    .locationName("USA")
                    .build()
            );
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error getting indicator data for {}: {}", indicatorId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/processing/{processingJobId}/quality-report")
    public ResponseEntity<DataQualityReport> getDataQualityReport(@PathVariable Long processingJobId) {
        try {
            // This would typically generate a quality report from the processing results
            DataQualityReport report = DataQualityReport.builder()
                    .totalRecords(1000L)
                    .validRecords(950L)
                    .errorRecords(30L)
                    .warningRecords(20L)
                    .qualityScore(0.95)
                    .warnings(List.of("Some values are outside expected range"))
                    .errors(List.of("Missing required fields in 30 records"))
                    .build();
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error getting quality report for job {}: {}", processingJobId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
} 