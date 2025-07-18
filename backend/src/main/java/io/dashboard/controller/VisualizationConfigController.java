package io.dashboard.controller;

import io.dashboard.dto.VisualizationConfigRequest;
import io.dashboard.dto.VisualizationConfigResponse;
import io.dashboard.service.VisualizationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@RestController
@RequestMapping("/api/v1/visualization-configs")
public class VisualizationConfigController {

    @Autowired
    private VisualizationConfigService visualizationConfigService;

    @GetMapping("/indicators/{indicatorId}")
    @PermitAll
    public ResponseEntity<List<VisualizationConfigResponse>> getConfigsForIndicator(
            @PathVariable Long indicatorId) {
        
        List<VisualizationConfigResponse> configs = visualizationConfigService.findByIndicatorId(indicatorId);
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<VisualizationConfigResponse> getConfigById(
            @PathVariable Long id) {
        
        // This would need to be implemented in the service
        // For now, we'll return a 404
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<VisualizationConfigResponse> createConfig(
            @RequestBody VisualizationConfigRequest request) {
        
        VisualizationConfigResponse response = visualizationConfigService.createConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<VisualizationConfigResponse> updateConfig(
            @PathVariable Long id,
            @RequestBody VisualizationConfigRequest request) {
        
        VisualizationConfigResponse response = visualizationConfigService.updateConfig(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteConfig(
            @PathVariable Long id) {
        
        visualizationConfigService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/set-default")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<VisualizationConfigResponse> setAsDefault(
            @PathVariable Long id) {
        
        VisualizationConfigResponse response = visualizationConfigService.setAsDefault(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/clone")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<VisualizationConfigResponse> cloneConfig(
            @PathVariable Long id,
            @RequestParam String newTitle) {
        
        VisualizationConfigResponse response = visualizationConfigService.cloneConfig(id, newTitle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 