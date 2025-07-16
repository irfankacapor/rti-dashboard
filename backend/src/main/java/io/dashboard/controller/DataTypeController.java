package io.dashboard.controller;

import io.dashboard.dto.DataTypeCreateRequest;
import io.dashboard.dto.DataTypeResponse;
import io.dashboard.dto.DataTypeUpdateRequest;
import io.dashboard.service.DataTypeService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DataTypeController {
    private final DataTypeService dataTypeService;

    @GetMapping("/data-types")
    @PermitAll
    public List<DataTypeResponse> getAllDataTypes() {
        return dataTypeService.findAll();
    }

    @GetMapping("/data-types/{id}")
    @PermitAll
    public DataTypeResponse getDataTypeById(@PathVariable Long id) {
        return dataTypeService.findById(id);
    }

    @PostMapping("/data-types")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<DataTypeResponse> createDataType(@Valid @RequestBody DataTypeCreateRequest request) {
        DataTypeResponse response = dataTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/data-types/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public DataTypeResponse updateDataType(@PathVariable Long id, @Valid @RequestBody DataTypeUpdateRequest request) {
        return dataTypeService.update(id, request);
    }

    @DeleteMapping("/data-types/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteDataType(@PathVariable Long id) {
        dataTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 