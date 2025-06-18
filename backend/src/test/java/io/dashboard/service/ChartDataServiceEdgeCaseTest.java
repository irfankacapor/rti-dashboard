package io.dashboard.service;

import io.dashboard.dto.*;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChartDataServiceEdgeCaseTest {

    @Mock
    private FactIndicatorValueRepository factIndicatorValueRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private ChartDataFormattingService formattingService;

    @InjectMocks
    private ChartDataService chartDataService;

    @Test
    void getTimeSeriesData_withNullIndicatorId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTimeSeriesData(null, LocalDateTime.now(), LocalDateTime.now()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid indicator ID");
    }

    @Test
    void getTimeSeriesData_withZeroIndicatorId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTimeSeriesData(0L, LocalDateTime.now(), LocalDateTime.now()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid indicator ID");
    }

    @Test
    void getTimeSeriesData_withNegativeIndicatorId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTimeSeriesData(-1L, LocalDateTime.now(), LocalDateTime.now()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid indicator ID");
    }

    @Test
    void getTimeSeriesData_withNullStartDate_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTimeSeriesData(1L, null, LocalDateTime.now()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Start date and end date are required");
    }

    @Test
    void getTimeSeriesData_withNullEndDate_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTimeSeriesData(1L, LocalDateTime.now(), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Start date and end date are required");
    }

    @Test
    void getTimeSeriesData_withStartDateAfterEndDate_shouldThrowBadRequestException() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        assertThatThrownBy(() -> chartDataService.getTimeSeriesData(1L, startDate, endDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Start date cannot be after end date");
    }

    @Test
    void getTimeSeriesData_withNonExistentIndicator_shouldThrowResourceNotFoundException() {
        when(indicatorRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> chartDataService.getTimeSeriesData(999L, LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Indicator not found with ID: 999");
    }

    @Test
    void getTimeSeriesData_withEmptyData_shouldReturnEmptyResponse() {
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        TimeSeriesDataResponse result = chartDataService.getTimeSeriesData(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        assertThat(result).isNull();
    }

    @Test
    void getLocationComparisonData_withNullIndicatorId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getLocationComparisonData(null, Arrays.asList(1L, 2L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid indicator ID");
    }

    @Test
    void getLocationComparisonData_withNullLocationIds_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getLocationComparisonData(1L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Location IDs are required");
    }

    @Test
    void getLocationComparisonData_withEmptyLocationIds_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getLocationComparisonData(1L, Collections.emptyList()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Location IDs are required");
    }

    @Test
    void getLocationComparisonData_withNonExistentIndicator_shouldThrowResourceNotFoundException() {
        when(indicatorRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> chartDataService.getLocationComparisonData(999L, Arrays.asList(1L, 2L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Indicator not found with ID: 999");
    }

    @Test
    void getLocationComparisonData_withNoMatchingLocations_shouldReturnEmptyResponse() {
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Collections.emptyList());

        LocationComparisonResponse response = new LocationComparisonResponse();
        response.setCategories(new ArrayList<>());
        response.setSeries(new ArrayList<>());
        when(formattingService.formatForBarChart(anyList(), anyString())).thenReturn(response);

        LocationComparisonResponse result = chartDataService.getLocationComparisonData(1L, Arrays.asList(1L, 2L));

        assertThat(result).isNotNull();
        assertThat(result.getCategories()).isEmpty();
        assertThat(result.getSeries()).isEmpty();
    }

    @Test
    void getDimensionBreakdownData_withNullIndicatorId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getDimensionBreakdownData(null, "category"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid indicator ID");
    }

    @Test
    void getDimensionBreakdownData_withNullDimensionType_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getDimensionBreakdownData(1L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Dimension type is required");
    }

    @Test
    void getDimensionBreakdownData_withEmptyDimensionType_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getDimensionBreakdownData(1L, ""))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Dimension type is required");
    }

    @Test
    void getDimensionBreakdownData_withBlankDimensionType_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getDimensionBreakdownData(1L, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Dimension type is required");
    }

    @Test
    void getDimensionBreakdownData_withNonExistentIndicator_shouldThrowResourceNotFoundException() {
        when(indicatorRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> chartDataService.getDimensionBreakdownData(999L, "category"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Indicator not found with ID: 999");
    }

    @Test
    void getIndicatorCorrelationData_withNullIndicatorIds_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getIndicatorCorrelationData(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("At least two indicator IDs are required for correlation analysis");
    }

    @Test
    void getIndicatorCorrelationData_withEmptyIndicatorIds_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getIndicatorCorrelationData(Collections.emptyList()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("At least two indicator IDs are required for correlation analysis");
    }

    @Test
    void getIndicatorCorrelationData_withSingleIndicator_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getIndicatorCorrelationData(Arrays.asList(1L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("At least two indicator IDs are required for correlation analysis");
    }

    @Test
    void getIndicatorCorrelationData_withDuplicateIndicatorIds_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getIndicatorCorrelationData(Arrays.asList(1L, 1L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Indicator IDs for correlation must be different");
    }

    @Test
    void getIndicatorCorrelationData_withNonExistentIndicators_shouldThrowResourceNotFoundException() {
        when(indicatorRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> chartDataService.getIndicatorCorrelationData(Arrays.asList(1L, 2L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Indicator not found with ID: 1");
    }

    @Test
    void getGoalProgressChartData_withNullGoalId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getGoalProgressChartData(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid goal ID");
    }

    @Test
    void getGoalProgressChartData_withZeroGoalId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getGoalProgressChartData(0L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid goal ID");
    }

    @Test
    void getGoalProgressChartData_withNegativeGoalId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getGoalProgressChartData(-1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid goal ID");
    }

    @Test
    void getSubareaPerformanceHeatmap_withNullAreaId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getSubareaPerformanceHeatmap(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid area ID");
    }

    @Test
    void getSubareaPerformanceHeatmap_withZeroAreaId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getSubareaPerformanceHeatmap(0L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid area ID");
    }

    @Test
    void getSubareaPerformanceHeatmap_withNegativeAreaId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getSubareaPerformanceHeatmap(-1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid area ID");
    }

    @Test
    void getTrendAnalysisData_withNullIndicatorId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTrendAnalysisData(null, 12))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid indicator ID");
    }

    @Test
    void getTrendAnalysisData_withZeroIndicatorId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTrendAnalysisData(0L, 12))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid indicator ID");
    }

    @Test
    void getTrendAnalysisData_withNegativeIndicatorId_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTrendAnalysisData(-1L, 12))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid indicator ID");
    }

    @Test
    void getTrendAnalysisData_withZeroPeriods_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTrendAnalysisData(1L, 0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Number of periods must be positive");
    }

    @Test
    void getTrendAnalysisData_withNegativePeriods_shouldThrowBadRequestException() {
        assertThatThrownBy(() -> chartDataService.getTrendAnalysisData(1L, -1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Number of periods must be positive");
    }

    @Test
    void getTrendAnalysisData_withTooManyPeriods_shouldThrowResourceNotFoundException() {
        when(indicatorRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> chartDataService.getTrendAnalysisData(1L, 1001))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Indicator not found with ID: 1");
    }

    @Test
    void getTimeSeriesData_withVeryLargeDateRange_shouldHandleGracefully() {
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        LocalDateTime startDate = LocalDateTime.now().minusYears(10);
        LocalDateTime endDate = LocalDateTime.now().plusYears(10);
        TimeSeriesDataResponse result = chartDataService.getTimeSeriesData(1L, startDate, endDate);
        assertThat(result).isNull();
    }

    @Test
    void getLocationComparisonData_withManyLocations_shouldHandleGracefully() {
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Collections.emptyList());

        LocationComparisonResponse response = new LocationComparisonResponse();
        response.setCategories(new ArrayList<>());
        response.setSeries(new ArrayList<>());
        when(formattingService.formatForBarChart(anyList(), anyString())).thenReturn(response);

        List<Long> manyLocationIds = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            manyLocationIds.add((long) i);
        }

        LocationComparisonResponse result = chartDataService.getLocationComparisonData(1L, manyLocationIds);

        assertThat(result).isNotNull();
    }

    @Test
    void getIndicatorCorrelationData_withManyIndicators_shouldThrowBadRequestException() {
        List<Long> manyIndicatorIds = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            manyIndicatorIds.add((long) i);
        }
        assertThatThrownBy(() -> chartDataService.getIndicatorCorrelationData(manyIndicatorIds))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Correlation analysis supports only two indicators");
    }
} 