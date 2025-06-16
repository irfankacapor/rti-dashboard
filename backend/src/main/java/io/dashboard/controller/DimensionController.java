package io.dashboard.controller;

import io.dashboard.dto.DimensionMappingRequest;
import io.dashboard.dto.DimensionMappingResponse;
import io.dashboard.dto.DimensionValidationResponse;
import io.dashboard.dto.MultiDimensionalAnalysis;
import io.dashboard.model.DimensionType;
import io.dashboard.service.DimensionMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class DimensionController {
    
    private final DimensionMappingService dimensionMappingService;
    
    @PostMapping("/uploads/{jobId}/dimension-mapping")
    public ResponseEntity<DimensionMappingResponse> createColumnDimensionMapping(
            @PathVariable Long jobId,
            @RequestBody DimensionMappingRequest request) {
        
        try {
            DimensionMappingResponse response = dimensionMappingService.createColumnMapping(
                    jobId, 
                    request.getColumnIndex(), 
                    request.getDimensionType(), 
                    request.getMappingRules()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating dimension mapping for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/uploads/{jobId}/dimension-suggestions")
    public ResponseEntity<List<DimensionMappingResponse>> getDimensionSuggestions(@PathVariable Long jobId) {
        
        try {
            List<DimensionMappingResponse> suggestions = dimensionMappingService.suggestDimensionMappings(jobId);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error getting dimension suggestions for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/uploads/{jobId}/validate-mappings")
    public ResponseEntity<DimensionValidationResponse> validateDimensionMappings(@PathVariable Long jobId) {
        
        try {
            DimensionValidationResponse response = dimensionMappingService.validateMappings(jobId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating dimension mappings for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/uploads/{jobId}/multi-dimensional-analysis")
    public ResponseEntity<MultiDimensionalAnalysis> getMultiDimensionalAnalysis(@PathVariable Long jobId) {
        
        try {
            MultiDimensionalAnalysis analysis = dimensionMappingService.processMultiDimensionalData(jobId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error analyzing multi-dimensional data for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/dimension-types")
    public ResponseEntity<DimensionType[]> getDimensionTypes() {
        return ResponseEntity.ok(DimensionType.values());
    }
} 