package io.dashboard.service;

import io.dashboard.dto.*;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Indicator;
import io.dashboard.model.DimTime;
import io.dashboard.model.DimLocation;
import io.dashboard.model.DimGeneric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChartDataFormattingServiceTest {

    private ChartDataFormattingService service;
    private FactIndicatorValue fact;
    private FactIndicatorValue fact2;
    private Indicator indicator;
    private DimTime time;
    private DimLocation location;
    private DimGeneric generic;

    @BeforeEach
    void setUp() {
        service = new ChartDataFormattingService();
        indicator = new Indicator();
        indicator.setId(1L);
        indicator.setName("Test Indicator");
        time = new DimTime();
        time.setId(1L);
        time.setValue("2023");
        time.setYear(2023);
        time.setMonth(1);
        location = new DimLocation();
        location.setId(1L);
        location.setName("Location A");
        generic = new DimGeneric();
        generic.setId(1L);
        generic.setValue("Category A");
        fact = new FactIndicatorValue();
        fact.setId(1L);
        fact.setIndicator(indicator);
        fact.setTime(time);
        fact.setLocation(location);
        fact.setGenerics(java.util.Collections.singletonList(generic));
        fact.setValue(BigDecimal.valueOf(100.0));
        fact2 = new FactIndicatorValue();
        fact2.setId(2L);
        fact2.setIndicator(indicator);
        fact2.setTime(time);
        fact2.setLocation(location);
        fact2.setGenerics(java.util.Collections.singletonList(generic));
        fact2.setValue(BigDecimal.valueOf(200.0));
    }

    @Test
    void formatForLineChart_withTimeSeriesData_shouldFormatCorrectly() {
        List<FactIndicatorValue> data = Arrays.asList(fact, fact2);
        TimeSeriesDataResponse response = service.formatForLineChart(data);
        assertNotNull(response);
        assertFalse(response.getLabels().isEmpty());
        assertFalse(response.getDatasets().isEmpty());
    }

    @Test
    void formatForLineChart_withEmptyData_shouldReturnEmpty() {
        TimeSeriesDataResponse response = service.formatForLineChart(Collections.emptyList());
        assertNotNull(response);
        assertTrue(response.getLabels().isEmpty());
        assertTrue(response.getDatasets().isEmpty());
    }

    @Test
    void formatForBarChart_withCategoryData_shouldFormatCorrectly() {
        List<FactIndicatorValue> data = Arrays.asList(fact, fact2);
        LocationComparisonResponse response = service.formatForBarChart(data, "location");
        assertNotNull(response);
        assertFalse(response.getCategories().isEmpty());
        assertFalse(response.getSeries().isEmpty());
    }

    @Test
    void formatForBarChart_withEmptyData_shouldReturnEmpty() {
        LocationComparisonResponse response = service.formatForBarChart(Collections.emptyList(), "location");
        assertNotNull(response);
        assertTrue(response.getCategories().isEmpty());
        assertTrue(response.getSeries().isEmpty());
    }

    @Test
    void formatForPieChart_withPercentageData_shouldFormatCorrectly() {
        List<FactIndicatorValue> data = Arrays.asList(fact, fact2);
        DimensionBreakdownResponse response = service.formatForPieChart(data, "category");
        assertNotNull(response);
        assertFalse(response.getLabels().isEmpty());
        assertFalse(response.getData().isEmpty());
        assertFalse(response.getColors().isEmpty());
    }

    @Test
    void formatForPieChart_withEmptyData_shouldReturnEmpty() {
        DimensionBreakdownResponse response = service.formatForPieChart(Collections.emptyList(), "category");
        assertNotNull(response);
        assertTrue(response.getLabels().isEmpty());
        assertTrue(response.getData().isEmpty());
        assertTrue(response.getColors().isEmpty());
    }

    @Test
    void formatForScatterPlot_withTwoDatasets_shouldFormatCorrectly() {
        List<FactIndicatorValue> xData = Arrays.asList(fact);
        List<FactIndicatorValue> yData = Arrays.asList(fact2);
        CorrelationDataResponse response = service.formatForScatterPlot(xData, yData);
        assertNotNull(response);
        assertNotNull(response.getDatasets());
    }

    @Test
    void formatForScatterPlot_withEmptyData_shouldReturnEmpty() {
        CorrelationDataResponse response = service.formatForScatterPlot(Collections.emptyList(), Collections.emptyList());
        assertNotNull(response);
        assertTrue(response.getDatasets().isEmpty());
    }

    @Test
    void formatForAreaChart_withData_shouldFormatCorrectly() {
        List<FactIndicatorValue> data = Arrays.asList(fact, fact2);
        TimeSeriesDataResponse response = service.formatForAreaChart(data);
        assertNotNull(response);
        assertFalse(response.getLabels().isEmpty());
        assertFalse(response.getDatasets().isEmpty());
        assertTrue(response.getDatasets().get(0).isFill());
    }

    @Test
    void formatForHeatmap_withMatrixData_shouldFormatCorrectly() {
        List<FactIndicatorValue> data = Arrays.asList(fact, fact2);
        HeatmapDataResponse response = service.formatForHeatmap(data, "x", "y");
        assertNotNull(response);
        assertNotNull(response.getXLabels());
        assertNotNull(response.getYLabels());
        assertNotNull(response.getData());
        assertNotNull(response.getColorScale());
    }

    @Test
    void formatForHeatmap_withEmptyData_shouldReturnEmpty() {
        HeatmapDataResponse response = service.formatForHeatmap(Collections.emptyList(), "x", "y");
        assertNotNull(response);
        assertTrue(response.getXLabels().isEmpty());
        assertTrue(response.getYLabels().isEmpty());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void formatForGauge_withCurrentAndTarget_shouldFormatCorrectly() {
        DimensionBreakdownResponse response = service.formatForGauge(fact, fact2);
        assertNotNull(response);
        assertEquals(2, response.getLabels().size());
        assertEquals(2, response.getData().size());
    }

    @Test
    void formatForGauge_withNulls_shouldReturnZeroes() {
        DimensionBreakdownResponse response = service.formatForGauge(null, null);
        assertNotNull(response);
        assertEquals(2, response.getLabels().size());
        assertEquals(2, response.getData().size());
        assertEquals(0.0, response.getData().get(0));
    }

    @Test
    void calculateTrendAnalysis_withSufficientData_shouldReturnTrend() {
        List<FactIndicatorValue> data = Arrays.asList(fact, fact2, fact, fact2, fact);
        TrendAnalysisResponse response = service.calculateTrendAnalysis(data, 3);
        assertNotNull(response);
        assertNotNull(response.getHistorical());
        assertNotNull(response.getProjected());
        assertNotNull(response.getTrendLine());
    }

    @Test
    void calculateTrendAnalysis_withInsufficientData_shouldThrowException() {
        List<FactIndicatorValue> data = Arrays.asList(fact);
        assertThrows(IllegalArgumentException.class, () -> service.calculateTrendAnalysis(data, 2));
    }

    @Test
    void applyColorScheme_withValidScheme_shouldApplyColors() {
        ChartData chartData = new ChartData();
        chartData.setLabels(Arrays.asList("A", "B"));
        chartData.setData(Arrays.asList(1.0, 2.0));
        chartData.setColor("#FF0000");
        ChartData result = service.applyColorScheme(chartData, "default");
        assertNotNull(result);
        assertEquals(chartData, result);
    }

    @Test
    void aggregateDataByPeriod_MONTHLY_shouldAggregateByMonth() {
        List<FactIndicatorValue> data = Arrays.asList(fact, fact2);
        List<FactIndicatorValue> result = service.aggregateDataByPeriod(data, "MONTHLY");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void aggregateDataByPeriod_YEARLY_shouldAggregateByYear() {
        List<FactIndicatorValue> data = Arrays.asList(fact, fact2);
        List<FactIndicatorValue> result = service.aggregateDataByPeriod(data, "YEARLY");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void aggregateDataByPeriod_withEmptyData_shouldReturnEmpty() {
        List<FactIndicatorValue> result = service.aggregateDataByPeriod(Collections.emptyList(), "MONTHLY");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
} 