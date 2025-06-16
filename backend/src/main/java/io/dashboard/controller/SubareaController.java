package io.dashboard.controller;

import io.dashboard.dto.SubareaCreateRequest;
import io.dashboard.dto.SubareaResponse;
import io.dashboard.dto.SubareaUpdateRequest;
import io.dashboard.service.SubareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SubareaController {
    private final SubareaService subareaService;

    @GetMapping("/subareas")
    public List<SubareaResponse> getAllSubareas() {
        return subareaService.findAll();
    }

    @GetMapping("/subareas/{id}")
    public SubareaResponse getSubareaById(@PathVariable Long id) {
        return subareaService.findById(id);
    }

    @GetMapping("/areas/{areaId}/subareas")
    public List<SubareaResponse> getSubareasByArea(@PathVariable Long areaId) {
        return subareaService.findByAreaId(areaId);
    }

    @PostMapping("/subareas")
    public ResponseEntity<SubareaResponse> createSubarea(@Valid @RequestBody SubareaCreateRequest request) {
        SubareaResponse response = subareaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/subareas/{id}")
    public SubareaResponse updateSubarea(@PathVariable Long id, @Valid @RequestBody SubareaUpdateRequest request) {
        return subareaService.update(id, request);
    }

    @DeleteMapping("/subareas/{id}")
    public ResponseEntity<Void> deleteSubarea(@PathVariable Long id) {
        subareaService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 