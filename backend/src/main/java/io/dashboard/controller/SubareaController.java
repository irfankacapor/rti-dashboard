package io.dashboard.controller;

import io.dashboard.dto.SubareaCreateRequest;
import io.dashboard.dto.SubareaResponse;
import io.dashboard.dto.SubareaUpdateRequest;
import io.dashboard.service.SubareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class SubareaController {
    private final SubareaService subareaService;

    @GetMapping("/subareas")
    @PermitAll
    public List<SubareaResponse> getAllSubareas() {
        return subareaService.findAll();
    }

    @GetMapping("/subareas/{id}")
    @PermitAll
    public SubareaResponse getSubareaById(@PathVariable Long id) {
        return subareaService.findById(id);
    }

    @GetMapping("/areas/{areaId}/subareas")
    @PermitAll
    public List<SubareaResponse> getSubareasByArea(@PathVariable Long areaId) {
        return subareaService.findByAreaId(areaId);
    }

    @PostMapping("/subareas")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<SubareaResponse> createSubarea(@Valid @RequestBody SubareaCreateRequest request) {
        SubareaResponse response = subareaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/subareas/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public SubareaResponse updateSubarea(@PathVariable Long id, @Valid @RequestBody SubareaUpdateRequest request) {
        return subareaService.update(id, request);
    }

    @DeleteMapping("/subareas/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteSubarea(@PathVariable Long id) {
        subareaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subareas/{id}/with-data")
    public ResponseEntity<Void> deleteSubareaWithData(@PathVariable Long id) {
        subareaService.deleteWithData(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subareas/{id}/aggregated-value")
    @PermitAll
    public ResponseEntity<Map<String, Object>> getAggregatedValue(@PathVariable Long id) {
        try {
            double aggregatedValue = subareaService.calculateAggregatedValue(id);
            Map<String, Object> response = Map.of(
                "subareaId", id,
                "aggregatedValue", aggregatedValue
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating aggregated value for subarea: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/subareas/{id}/aggregated-by-time")
    @PermitAll
    public ResponseEntity<Map<String, Object>> getAggregatedByTime(@PathVariable Long id) {
        try {
            Map<String, Double> timeData = subareaService.getAggregatedByTime(id);
            Map<String, Object> response = Map.of(
                "subareaId", id,
                "dimension", "time",
                "data", timeData
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating aggregated by time for subarea: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/subareas/{id}/aggregated-by-location")
    @PermitAll
    public ResponseEntity<Map<String, Object>> getAggregatedByLocation(@PathVariable Long id) {
        try {
            Map<String, Double> locationData = subareaService.getAggregatedByLocation(id);
            Map<String, Object> response = Map.of(
                "subareaId", id,
                "dimension", "location",
                "data", locationData
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating aggregated by location for subarea: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/subareas/{id}/sample-data")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Map<String, Object>> createSampleData(@PathVariable Long id) {
        try {
            // This is a simple test endpoint to create sample data
            // In a real application, this would be done through proper data import
            Map<String, Object> response = Map.of(
                "subareaId", id,
                "message", "Sample data creation endpoint - implement proper data import logic",
                "status", "success"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/subareas/{id}/test")
    @PermitAll
    public ResponseEntity<Map<String, Object>> testSubarea(@PathVariable Long id) {
        try {
            log.info("Testing subarea endpoint for ID: {}", id);
            
            // Check if subarea exists
            boolean exists = subareaService.existsById(id);
            
            Map<String, Object> response = Map.of(
                "subareaId", id,
                "exists", exists,
                "message", "Test endpoint working"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in test endpoint for subarea: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/subareas/{id}/aggregated-by-{dimension}")
    @PermitAll
    public ResponseEntity<Map<String, Object>> getAggregatedByDimension(@PathVariable Long id, @PathVariable String dimension) {
        try {
            Map<String, Double> data = subareaService.getAggregatedByDimension(id, dimension);
            Map<String, Object> response = Map.of(
                "subareaId", id,
                "dimension", dimension,
                "data", data
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating aggregated by {} for subarea: {}", dimension, id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 