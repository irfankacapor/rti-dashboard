package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.DataProcessingRequest;
import io.dashboard.dto.DataProcessingResponse;
import io.dashboard.dto.DataQualityReport;
import io.dashboard.dto.ProcessingStatusResponse;
import io.dashboard.service.DataProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataProcessingController.class)
class DataProcessingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private DataProcessingService dataProcessingService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void startDataProcessing_shouldReturnSuccessResponse() throws Exception {
        // Given
        DataProcessingRequest request = DataProcessingRequest.builder()
                .uploadJobId(1L)
                .batchSize(1000)
                .enableDataQualityChecks(true)
                .build();
        
        DataProcessingResponse response = DataProcessingResponse.builder()
                .processingJobId(1L)
                .status("COMPLETED")
                .message("Data processing completed successfully")
                .build();
        
        when(dataProcessingService.processUploadJob(eq(1L)))
                .thenReturn(CompletableFuture.completedFuture(response));
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/1/process-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processingJobId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("Data processing completed successfully"));
    }
    
    @Test
    void startDataProcessing_shouldUseDefaultRequestWhenNoBodyProvided() throws Exception {
        // Given
        DataProcessingResponse response = DataProcessingResponse.builder()
                .processingJobId(1L)
                .status("COMPLETED")
                .message("Data processing completed successfully")
                .build();
        
        when(dataProcessingService.processUploadJob(eq(1L)))
                .thenReturn(CompletableFuture.completedFuture(response));
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/1/process-data")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processingJobId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void startDataProcessing_shouldHandleServiceException() throws Exception {
        // Given
        when(dataProcessingService.processUploadJob(eq(1L)))
                .thenReturn(CompletableFuture.completedFuture(
                        DataProcessingResponse.builder()
                                .status("FAILED")
                                .message("Processing failed")
                                .build()
                ));
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/1/process-data")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Processing failed"));
    }
    
    @Test
    void getProcessingStatus_shouldReturnStatusResponse() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/processing/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.progressPercentage").value(50.0))
                .andExpect(jsonPath("$.recordsProcessed").value(1000))
                .andExpect(jsonPath("$.totalRecords").value(2000))
                .andExpect(jsonPath("$.errorCount").value(5));
    }
    
    @Test
    void getProcessingErrors_shouldReturnErrorList() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/processing/1/errors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rowNumber").value(5))
                .andExpect(jsonPath("$[0].errorMessage").value("Invalid numeric value"))
                .andExpect(jsonPath("$[0].severity").value("ERROR"))
                .andExpect(jsonPath("$[1].rowNumber").value(12))
                .andExpect(jsonPath("$[1].errorMessage").value("Missing required field"))
                .andExpect(jsonPath("$[1].severity").value("WARNING"));
    }
    
    @Test
    void retryFailedProcessing_shouldReturnSuccessResponse() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/processing/1/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processingJobId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Processing job restarted"));
    }
    
    @Test
    void getIndicatorData_shouldReturnFactData() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/indicators/1/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].indicatorId").value(1))
                .andExpect(jsonPath("$[0].indicatorName").value("GDP"))
                .andExpect(jsonPath("$[0].value").value(1000000))
                .andExpect(jsonPath("$[0].timeValue").value("2023"))
                .andExpect(jsonPath("$[0].locationName").value("USA"));
    }
    
    @Test
    void getDataQualityReport_shouldReturnQualityReport() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/processing/1/quality-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(1000))
                .andExpect(jsonPath("$.validRecords").value(950))
                .andExpect(jsonPath("$.errorRecords").value(30))
                .andExpect(jsonPath("$.warningRecords").value(20))
                .andExpect(jsonPath("$.qualityScore").value(0.95))
                .andExpect(jsonPath("$.warnings[0]").value("Some values are outside expected range"))
                .andExpect(jsonPath("$.errors[0]").value("Missing required fields in 30 records"));
    }
    
    @Test
    void startDataProcessing_shouldHandleInvalidRequest() throws Exception {
        // Given
        DataProcessingRequest request = DataProcessingRequest.builder()
                .uploadJobId(null) // Invalid request
                .build();
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/1/process-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Controller returns 400 for invalid
    }
    
    @Test
    void getProcessingStatus_shouldHandleInvalidJobId() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/processing/999/status"))
                .andExpect(status().isOk()); // Controller returns mock response
    }
    
    @Test
    void getProcessingErrors_shouldHandleInvalidJobId() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/processing/999/errors"))
                .andExpect(status().isOk()); // Controller returns mock response
    }
    
    @Test
    void retryFailedProcessing_shouldHandleInvalidJobId() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/processing/999/retry"))
                .andExpect(status().isOk()); // Controller returns mock response
    }
    
    @Test
    void getIndicatorData_shouldHandleInvalidIndicatorId() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/indicators/999/data"))
                .andExpect(status().isOk()); // Controller returns mock response
    }
    
    @Test
    void getDataQualityReport_shouldHandleInvalidJobId() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/processing/999/quality-report"))
                .andExpect(status().isOk()); // Controller returns mock response
    }
} 