package io.dashboard.controller;

import io.dashboard.dto.IndicatorBatchRequest;
import io.dashboard.dto.IndicatorBatchResponse;
import io.dashboard.service.IndicatorBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IndicatorBatchController {

    private final IndicatorBatchService indicatorBatchService;

    @PostMapping("/indicators/create-from-csv")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<IndicatorBatchResponse> createIndicatorsFromCsv(
            @RequestBody @Valid IndicatorBatchRequest request) {
        
        IndicatorBatchResponse response = indicatorBatchService.createFromCsvData(request);
        return ResponseEntity.ok(response);
    }
} 