package io.dashboard.controller;

import io.dashboard.dto.CsvPreviewResponse;
import io.dashboard.dto.CsvStructureResponse;
import io.dashboard.model.UploadJob;
import io.dashboard.repository.UploadJobRepository;
import io.dashboard.service.CsvParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class CsvParsingController {
    
    private final CsvParsingService csvParsingService;
    private final UploadJobRepository uploadJobRepository;
    
    @GetMapping("/uploads/{jobId}/csv-preview")
    public ResponseEntity<CsvPreviewResponse> getCsvPreview(
            @PathVariable Long jobId,
            @RequestParam String filename) {
        
        try {
            CsvPreviewResponse preview = csvParsingService.parseAndPreview(jobId, filename);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            log.error("Error getting CSV preview for job {} and file {}: {}", jobId, filename, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/uploads/{jobId}/csv-structure")
    public ResponseEntity<CsvStructureResponse> getCsvStructure(
            @PathVariable Long jobId,
            @RequestParam String filename) {
        
        try {
            CsvStructureResponse structure = csvParsingService.analyzeCsvStructure(jobId, filename);
            return ResponseEntity.ok(structure);
        } catch (Exception e) {
            log.error("Error getting CSV structure for job {} and file {}: {}", jobId, filename, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/uploads/{jobId}/analyze-csv")
    public ResponseEntity<List<CsvStructureResponse>> analyzeAllCsvFiles(@PathVariable Long jobId) {
        
        // Check if job exists
        if (!uploadJobRepository.existsById(jobId)) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<CsvStructureResponse> analyses = csvParsingService.analyzeAllCsvFiles(jobId);
            return ResponseEntity.ok(analyses);
        } catch (Exception e) {
            log.error("Error analyzing all CSV files for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
} 