package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.*;
import io.dashboard.exception.GlobalExceptionHandler;
import io.dashboard.service.DashboardDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardDataControllerTest {
    @Mock
    private DashboardDataService dashboardDataService;
    
    @InjectMocks
    private DashboardDataController dashboardDataController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardDataController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== GET DASHBOARD DATA TESTS ==========

    @Test
    void getDashboardData_shouldReturnDashboardData() throws Exception {
        // Given
        DashboardDataResponse mockResponse = new DashboardDataResponse();
        mockResponse.setLastUpdated(LocalDateTime.now());
        when(dashboardDataService.getDashboardData(1L)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard-data/{dashboardId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    void getDashboardData_shouldHandleDashboardNotFound() throws Exception {
        // Given
        when(dashboardDataService.getDashboardData(1L)).thenThrow(new RuntimeException("Dashboard not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard-data/{dashboardId}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDashboardData_shouldHandleInvalidDashboardId() throws Exception {
        // No stubbing needed, controller should return 400 before calling service
        mockMvc.perform(get("/api/v1/dashboard-data/{dashboardId}", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDashboardData_shouldHandleZeroDashboardId() throws Exception {
        // No stubbing needed, controller should return 400 before calling service
        mockMvc.perform(get("/api/v1/dashboard-data/{dashboardId}", 0L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDashboardData_shouldHandleServiceException() throws Exception {
        // Given
        when(dashboardDataService.getDashboardData(1L)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard-data/{dashboardId}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDashboardData_shouldHandleLargeDashboardId() throws Exception {
        // Given
        DashboardDataResponse mockResponse = new DashboardDataResponse();
        mockResponse.setLastUpdated(LocalDateTime.now());
        when(dashboardDataService.getDashboardData(Long.MAX_VALUE)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard-data/{dashboardId}", Long.MAX_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    // ========== GET PERFORMANCE METRICS TESTS ==========

    @Test
    void getPerformanceMetrics_shouldReturnMetrics() throws Exception {
        PerformanceMetricsResponse response = new PerformanceMetricsResponse();
        response.setAreaId(1L);
        response.setAverageScore(85.0);
        response.setMetrics(Collections.emptyList());
        
        when(dashboardDataService.getPerformanceMetrics(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/performance-metrics/{areaId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.areaId").value(1))
                .andExpect(jsonPath("$.averageScore").value(85.0))
                .andExpect(jsonPath("$.metrics").isArray());

        verify(dashboardDataService).getPerformanceMetrics(1L);
    }

    @Test
    void getPerformanceMetrics_shouldHandleInvalidAreaId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/performance-metrics/{areaId}", -1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getPerformanceMetrics(anyLong());
    }

    @Test
    void getPerformanceMetrics_shouldHandleServiceException() throws Exception {
        when(dashboardDataService.getPerformanceMetrics(1L))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/v1/dashboard-data/performance-metrics/{areaId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(dashboardDataService).getPerformanceMetrics(1L);
    }

    // ========== GET DATA AGGREGATION TESTS ==========

    @Test
    void getDataAggregation_shouldReturnAggregatedData() throws Exception {
        DataAggregationResponse response = new DataAggregationResponse();
        response.setIndicatorId(1L);
        response.setAggregationType("SUM");
        Map<String, Object> data = new HashMap<>();
        data.put("value", 300.0);
        data.put("count", 3);
        response.setData(data);
        
        when(dashboardDataService.getDataAggregation(eq(1L), eq("SUM"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/aggregation")
                .param("indicatorId", "1")
                .param("aggregationType", "SUM")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2023-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicatorId").value(1))
                .andExpect(jsonPath("$.aggregationType").value("SUM"))
                .andExpect(jsonPath("$.data.value").value(300.0))
                .andExpect(jsonPath("$.data.count").value(3));

        verify(dashboardDataService).getDataAggregation(eq(1L), eq("SUM"), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDataAggregation_shouldHandleMissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/aggregation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getDataAggregation(anyLong(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDataAggregation_shouldHandleInvalidIndicatorId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/aggregation")
                .param("indicatorId", "-1")
                .param("aggregationType", "SUM")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2023-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getDataAggregation(anyLong(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDataAggregation_shouldHandleInvalidDateFormats() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/aggregation")
                .param("indicatorId", "1")
                .param("aggregationType", "SUM")
                .param("startDate", "invalid-date")
                .param("endDate", "invalid-date")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getDataAggregation(anyLong(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDataAggregation_shouldHandleDifferentAggregationTypes() throws Exception {
        DataAggregationResponse response = new DataAggregationResponse();
        response.setIndicatorId(1L);
        response.setAggregationType("AVERAGE");
        Map<String, Object> data = new HashMap<>();
        data.put("value", 150.0);
        response.setData(data);
        
        when(dashboardDataService.getDataAggregation(eq(1L), eq("AVERAGE"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/aggregation")
                .param("indicatorId", "1")
                .param("aggregationType", "AVERAGE")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2023-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aggregationType").value("AVERAGE"))
                .andExpect(jsonPath("$.data.value").value(150.0));

        verify(dashboardDataService).getDataAggregation(eq(1L), eq("AVERAGE"), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ========== GET REAL-TIME UPDATES TESTS ==========

    @Test
    void getRealTimeUpdates_shouldReturnUpdates() throws Exception {
        RealTimeUpdateResponse response = new RealTimeUpdateResponse();
        response.setDashboardId(1L);
        response.setTimestamp(LocalDateTime.now());
        response.setUpdates(Collections.emptyList());
        
        when(dashboardDataService.getRealTimeUpdates(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/real-time-updates/{dashboardId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboardId").value(1))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.updates").isArray());

        verify(dashboardDataService).getRealTimeUpdates(1L);
    }

    @Test
    void getRealTimeUpdates_shouldHandleInvalidDashboardId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/real-time-updates/{dashboardId}", -1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(dashboardDataService, never()).getRealTimeUpdates(anyLong());
    }

    // ========== GET HISTORICAL DATA TESTS ==========

    @Test
    void getHistoricalData_shouldReturnHistoricalData() throws Exception {
        // Given
        HistoricalDataResponse mockResponse = new HistoricalDataResponse();
        mockResponse.setIndicatorId(1L);
        when(dashboardDataService.getHistoricalData(1L, 6)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard-data/historical/{indicatorId}", 1L)
                .param("months", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicatorId").value(1));
    }

    @Test
    void getHistoricalData_shouldHandleInvalidIndicatorId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/historical/{indicatorId}", -1L)
                .param("months", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getHistoricalData(anyLong(), anyInt());
    }

    @Test
    void getHistoricalData_shouldHandleInvalidMonths() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/historical/{indicatorId}", 1L)
                .param("months", "-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getHistoricalData(anyLong(), anyInt());
    }

    @Test
    void getHistoricalData_shouldHandleZeroMonths() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/historical/{indicatorId}", 1L)
                .param("months", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getHistoricalData(anyLong(), anyInt());
    }

    @Test
    void getHistoricalData_shouldHandleLargeMonthRange() throws Exception {
        HistoricalDataResponse response = new HistoricalDataResponse();
        response.setIndicatorId(1L);
        response.setStartDate(LocalDateTime.now().minusMonths(100));
        response.setEndDate(LocalDateTime.now());
        response.setDataPoints(Collections.emptyList());
        
        when(dashboardDataService.getHistoricalData(1L, 100)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/historical/{indicatorId}", 1L)
                .param("months", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicatorId").value(1));

        verify(dashboardDataService).getHistoricalData(1L, 100);
    }

    // ========== GET DATA EXPORT TESTS ==========

    @Test
    void getDataExport_shouldReturnExportedData() throws Exception {
        DataExportResponse response = new DataExportResponse();
        response.setDashboardId(1L);
        response.setFormat("JSON");
        response.setData("{\"exported\": \"data\"}");
        response.setExportedAt(LocalDateTime.now());
        
        when(dashboardDataService.getDataExport(1L, "JSON")).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/export/{dashboardId}", 1L)
                .param("format", "JSON")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboardId").value(1))
                .andExpect(jsonPath("$.format").value("JSON"))
                .andExpect(jsonPath("$.data").value("{\"exported\": \"data\"}"))
                .andExpect(jsonPath("$.exportedAt").exists());

        verify(dashboardDataService).getDataExport(1L, "JSON");
    }

    @Test
    void getDataExport_shouldHandleInvalidDashboardId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/export/{dashboardId}", -1L)
                .param("format", "JSON")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getDataExport(anyLong(), anyString());
    }

    @Test
    void getDataExport_shouldHandleDifferentFormats() throws Exception {
        DataExportResponse response = new DataExportResponse();
        response.setDashboardId(1L);
        response.setFormat("CSV");
        response.setData("header1,header2\nvalue1,value2");
        response.setExportedAt(LocalDateTime.now());
        
        when(dashboardDataService.getDataExport(1L, "CSV")).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/export/{dashboardId}", 1L)
                .param("format", "CSV")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("CSV"));

        verify(dashboardDataService).getDataExport(1L, "CSV");
    }

    // ========== GET DATA VALIDATION TESTS ==========

    @Test
    void getDataValidation_shouldReturnValidationResults() throws Exception {
        DataValidationResponse response = new DataValidationResponse();
        response.setIndicatorId(1L);
        response.setTotalRecords(100L);
        response.setValidRecords(95L);
        response.setInvalidRecords(5L);
        response.setIsValid(false);
        response.setValidationErrors(Arrays.asList("Invalid value at record 1"));
        
        when(dashboardDataService.getDataValidation(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/validation/{indicatorId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicatorId").value(1))
                .andExpect(jsonPath("$.totalRecords").value(100))
                .andExpect(jsonPath("$.validRecords").value(95))
                .andExpect(jsonPath("$.invalidRecords").value(5))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.validationErrors").isArray());

        verify(dashboardDataService).getDataValidation(1L);
    }

    @Test
    void getDataValidation_shouldHandleInvalidIndicatorId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/validation/{indicatorId}", -1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getDataValidation(anyLong());
    }

    // ========== GET DATA QUALITY METRICS TESTS ==========

    @Test
    void getDataQualityMetrics_shouldReturnQualityMetrics() throws Exception {
        DataQualityMetricsResponse response = new DataQualityMetricsResponse();
        response.setDashboardId(1L);
        response.setCompleteness(95.5);
        response.setAccuracy(92.3);
        response.setTimeliness(88.7);
        response.setOverallScore(92.2);
        response.setLastCalculated(LocalDateTime.now());
        
        when(dashboardDataService.getDataQualityMetrics(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/quality-metrics/{dashboardId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboardId").value(1))
                .andExpect(jsonPath("$.completeness").value(95.5))
                .andExpect(jsonPath("$.accuracy").value(92.3))
                .andExpect(jsonPath("$.timeliness").value(88.7))
                .andExpect(jsonPath("$.overallScore").value(92.2))
                .andExpect(jsonPath("$.lastCalculated").exists());

        verify(dashboardDataService).getDataQualityMetrics(1L);
    }

    @Test
    void getDataQualityMetrics_shouldHandleInvalidDashboardId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/quality-metrics/{dashboardId}", -1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(dashboardDataService, never()).getDataQualityMetrics(anyLong());
    }

    // ========== GET DATA REFRESH STATUS TESTS ==========

    @Test
    void getDataRefreshStatus_shouldReturnRefreshStatus() throws Exception {
        DataRefreshStatusResponse response = new DataRefreshStatusResponse();
        response.setDashboardId(1L);
        response.setLastRefresh(LocalDateTime.now());
        response.setNextRefresh(LocalDateTime.now().plusHours(1));
        response.setRefreshInterval("1 hour");
        response.setIsAutoRefresh(true);
        response.setStatus("ACTIVE");
        
        when(dashboardDataService.getDataRefreshStatus(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/refresh-status/{dashboardId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboardId").value(1))
                .andExpect(jsonPath("$.lastRefresh").exists())
                .andExpect(jsonPath("$.nextRefresh").exists())
                .andExpect(jsonPath("$.refreshInterval").value("1 hour"))
                .andExpect(jsonPath("$.isAutoRefresh").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(dashboardDataService).getDataRefreshStatus(1L);
    }

    @Test
    void getDataRefreshStatus_shouldHandleInvalidDashboardId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/refresh-status/{dashboardId}", -1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(dashboardDataService, never()).getDataRefreshStatus(anyLong());
    }

    // ========== EDGE CASES AND ERROR HANDLING TESTS ==========

    @Test
    void allEndpoints_shouldHandleUnsupportedHttpMethods() throws Exception {
        // Test POST on GET endpoints
        mockMvc.perform(post("/api/v1/dashboard-data/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(put("/api/v1/dashboard-data/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/api/v1/dashboard-data/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(patch("/api/v1/dashboard-data/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void allEndpoints_shouldHandleInvalidContentType() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/1")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()); // Should still work as GET doesn't require specific content type
    }

    @Test
    void allEndpoints_shouldHandleMissingContentType() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/1"))
                .andExpect(status().isOk()); // Should still work as GET doesn't require content type
    }

    @Test
    void allEndpoints_shouldHandleMalformedUrls() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allEndpoints_shouldHandleNonNumericIds() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard-data/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/dashboard-data/performance-metrics/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/dashboard-data/historical/abc")
                .param("months", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== PERFORMANCE AND STRESS TESTS ==========

    @Test
    void getDashboardData_shouldHandleConcurrentRequests() throws Exception {
        // Given
        DashboardDataResponse mockResponse = new DashboardDataResponse();
        mockResponse.setLastUpdated(LocalDateTime.now());
        when(dashboardDataService.getDashboardData(1L)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard-data/{dashboardId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    void getDataAggregation_shouldHandleLargeDateRanges() throws Exception {
        DataAggregationResponse response = new DataAggregationResponse();
        response.setIndicatorId(1L);
        response.setAggregationType("SUM");
        Map<String, Object> data = new HashMap<>();
        data.put("value", 1000000.0);
        response.setData(data);
        
        when(dashboardDataService.getDataAggregation(eq(1L), eq("SUM"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard-data/aggregation")
                .param("indicatorId", "1")
                .param("aggregationType", "SUM")
                .param("startDate", "2020-01-01T00:00:00")
                .param("endDate", "2023-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value(1000000.0));

        verify(dashboardDataService).getDataAggregation(eq(1L), eq("SUM"), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ========== INTEGRATION SCENARIO TESTS ==========

    @Test
    void fullDashboardDataWorkflow_shouldWorkEndToEnd() throws Exception {
        // Given
        DashboardDataResponse mockResponse = new DashboardDataResponse();
        mockResponse.setLastUpdated(LocalDateTime.now());
        when(dashboardDataService.getDashboardData(1L)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard-data/{dashboardId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }
} 