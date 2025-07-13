package io.dashboard.controller;

import io.dashboard.dto.UnitCreateRequest;
import io.dashboard.dto.UnitResponse;
import io.dashboard.dto.UnitUpdateRequest;
import io.dashboard.service.UnitService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UnitController {
    private final UnitService unitService;

    @GetMapping("/units")
    @PermitAll
    public List<UnitResponse> getAllUnits() {
        return unitService.findAll();
    }

    @GetMapping("/units/grouped")
    @PermitAll
    public Map<String, List<UnitResponse>> getAllUnitsGrouped() {
        return unitService.findAllGrouped();
    }

    @GetMapping("/units/{id}")
    @PermitAll
    public UnitResponse getUnitById(@PathVariable Long id) {
        return unitService.findById(id);
    }

    @PostMapping("/units")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<UnitResponse> createUnit(@Valid @RequestBody UnitCreateRequest request) {
        UnitResponse response = unitService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/units/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public UnitResponse updateUnit(@PathVariable Long id, @Valid @RequestBody UnitUpdateRequest request) {
        return unitService.update(id, request);
    }

    @DeleteMapping("/units/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 