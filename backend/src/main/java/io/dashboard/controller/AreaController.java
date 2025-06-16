package io.dashboard.controller;

import io.dashboard.dto.AreaCreateRequest;
import io.dashboard.dto.AreaResponse;
import io.dashboard.dto.AreaUpdateRequest;
import io.dashboard.service.AreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaService areaService;

    @GetMapping
    public List<AreaResponse> getAllAreas() {
        return areaService.findAll();
    }

    @GetMapping("/{id}")
    public AreaResponse getAreaById(@PathVariable Long id) {
        return areaService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AreaResponse> createArea(@Valid @RequestBody AreaCreateRequest request) {
        AreaResponse response = areaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public AreaResponse updateArea(@PathVariable Long id, @Valid @RequestBody AreaUpdateRequest request) {
        return areaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable Long id) {
        areaService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 