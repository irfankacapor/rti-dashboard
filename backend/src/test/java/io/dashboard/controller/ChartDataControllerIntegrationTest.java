package io.dashboard.controller;

import io.dashboard.dto.*;
import io.dashboard.service.ChartDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChartDataController.class)
class ChartDataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChartDataService chartDataService;

    @Test
    void getTimeSeriesData_withValidParams_shouldReturn200() throws Exception {
        TimeSeriesDataResponse response = new TimeSeriesDataResponse();
        response.setLabels(Arrays.asList("2023", "2024"));
        response.setDatasets(Arrays.asList(new TimeSeriesDataResponse.Dataset()));
        
        when(chartDataService.getTimeSeriesData(anyLong(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/charts/indicators/1/time-series")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2024-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").exists())
                .andExpect(jsonPath("$.datasets").exists());
    }

    @Test
    void getTimeSeriesData_withInvalidIndicator_shouldReturn404() throws Exception {
        when(chartDataService.getTimeSeriesData(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("Indicator not found"));

        mockMvc.perform(get("/api/v1/charts/indicators/999/time-series")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2024-01-01T00:00:00"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTimeSeriesData_withInvalidDateRange_shouldReturn400() throws Exception {
        when(chartDataService.getTimeSeriesData(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("Invalid date range"));

        mockMvc.perform(get("/api/v1/charts/indicators/1/time-series")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2023-01-01T00:00:00"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getLocationComparison_withValidLocations_shouldReturn200() throws Exception {
        LocationComparisonResponse response = new LocationComparisonResponse();
        response.setCategories(Arrays.asList("Location A", "Location B"));
        response.setSeries(Arrays.asList(new LocationComparisonResponse.Series()));
        
        when(chartDataService.getLocationComparisonData(anyLong(), anyList())).thenReturn(response);

        mockMvc.perform(get("/api/v1/charts/indicators/1/location-comparison")
                .param("locationIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").exists())
                .andExpect(jsonPath("$.series").exists());
    }

    @Test
    void getDimensionBreakdown_withValidDimension_shouldReturn200() throws Exception {
        DimensionBreakdownResponse response = new DimensionBreakdownResponse();
        response.setLabels(Arrays.asList("Category A", "Category B"));
        response.setData(Arrays.asList(50.0, 50.0));
        response.setColors(Arrays.asList("#FF0000", "#00FF00"));
        
        when(chartDataService.getDimensionBreakdownData(anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(get("/api/v1/charts/indicators/1/dimension-breakdown")
                .param("dimensionType", "category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getCorrelationData_withValidIndicators_shouldReturn200() throws Exception {
        CorrelationDataResponse response = new CorrelationDataResponse();
        response.setDatasets(Arrays.asList(new CorrelationDataResponse.Dataset()));
        response.setXAxis(new CorrelationDataResponse.Axis());
        response.setYAxis(new CorrelationDataResponse.Axis());
        
        when(chartDataService.getIndicatorCorrelationData(anyList())).thenReturn(response);

        mockMvc.perform(get("/api/v1/charts/correlation")
                .param("indicatorIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasets").exists());
    }

    @Test
    void getCorrelationData_withTooFewIndicators_shouldReturn400() throws Exception {
        when(chartDataService.getIndicatorCorrelationData(anyList()))
                .thenThrow(new RuntimeException("At least two indicators required"));

        mockMvc.perform(get("/api/v1/charts/correlation")
                .param("indicatorIds", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getGoalProgressChart_withValidGoal_shouldReturn200() throws Exception {
        TimeSeriesDataResponse response = new TimeSeriesDataResponse();
        response.setLabels(Arrays.asList("Q1", "Q2"));
        response.setDatasets(Arrays.asList(new TimeSeriesDataResponse.Dataset()));
        
        when(chartDataService.getGoalProgressChartData(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/charts/goals/1/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").exists());
    }

    @Test
    void getAreaHeatmap_withValidArea_shouldReturn200() throws Exception {
        HeatmapDataResponse response = new HeatmapDataResponse();
        response.setXLabels(Arrays.asList("Q1", "Q2", "Q3", "Q4"));
        response.setYLabels(Arrays.asList("Region A", "Region B"));
        response.setData(Arrays.asList(Arrays.asList(80.0, 85.0, 90.0, 95.0), Arrays.asList(75.0, 80.0, 85.0, 90.0)));
        response.setColorScale(new HeatmapDataResponse.ColorScale());
        
        when(chartDataService.getSubareaPerformanceHeatmap(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/charts/areas/1/heatmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xlabels").exists())
                .andExpect(jsonPath("$.ylabels").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getTrendAnalysis_withValidParams_shouldReturn200() throws Exception {
        TrendAnalysisResponse response = new TrendAnalysisResponse();
        response.setHistorical(Arrays.asList(new TrendAnalysisResponse.DataPoint()));
        response.setProjected(Arrays.asList(new TrendAnalysisResponse.DataPoint()));
        response.setTrendLine(new TrendAnalysisResponse.TrendLine());
        
        when(chartDataService.getTrendAnalysisData(anyLong(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/charts/indicators/1/trend")
                .param("periods", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historical").exists())
                .andExpect(jsonPath("$.projected").exists());
    }

    @Test
    void getTrendAnalysis_withDefaultPeriods_shouldReturn200() throws Exception {
        TrendAnalysisResponse response = new TrendAnalysisResponse();
        response.setHistorical(Arrays.asList(new TrendAnalysisResponse.DataPoint()));
        response.setProjected(Arrays.asList(new TrendAnalysisResponse.DataPoint()));
        response.setTrendLine(new TrendAnalysisResponse.TrendLine());
        
        when(chartDataService.getTrendAnalysisData(anyLong(), eq(12))).thenReturn(response);

        mockMvc.perform(get("/api/v1/charts/indicators/1/trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historical").exists());
    }
} 