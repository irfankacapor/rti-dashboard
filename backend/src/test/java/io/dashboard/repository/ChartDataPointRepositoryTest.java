package io.dashboard.repository;

import io.dashboard.entity.ChartDataPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class ChartDataPointRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChartDataPointRepository chartDataPointRepository;

    @Test
    void findByConfigIdOrderByTimestamp_shouldReturnDataPoints() {
        // Given
        ChartDataPoint dataPoint1 = createChartDataPoint(1L, "2023", 100.0, LocalDateTime.now().minusDays(1));
        ChartDataPoint dataPoint2 = createChartDataPoint(1L, "2024", 110.0, LocalDateTime.now());

        // When
        List<ChartDataPoint> result = chartDataPointRepository.findByConfigIdOrderByTimestamp(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("yValue").containsExactly(100.0, 110.0);
    }

    @Test
    void findByConfigIdOrderByTimestamp_withNoData_shouldReturnEmptyList() {
        // When
        List<ChartDataPoint> result = chartDataPointRepository.findByConfigIdOrderByTimestamp(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByConfigIdAndTimestampBetween_shouldReturnDataPoints() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        ChartDataPoint dataPoint1 = createChartDataPoint(1L, "2023", 100.0, LocalDateTime.now().minusDays(1));
        ChartDataPoint dataPoint2 = createChartDataPoint(1L, "2024", 110.0, LocalDateTime.now());
        ChartDataPoint dataPoint3 = createChartDataPoint(1L, "2025", 120.0, LocalDateTime.now().plusDays(2)); // Outside range

        // When
        List<ChartDataPoint> result = chartDataPointRepository.findByConfigIdAndTimestampBetween(1L, startDate, endDate);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("yValue").containsExactlyInAnyOrder(100.0, 110.0);
    }

    @Test
    void findByConfigIdAndTimestampBetween_withNoDataInRange_shouldReturnEmptyList() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now().minusDays(5);

        // When
        List<ChartDataPoint> result = chartDataPointRepository.findByConfigIdAndTimestampBetween(1L, startDate, endDate);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByConfigIdAndTimestampBetween_withExactBoundaries_shouldIncludeBoundaryData() {
        LocalDateTime boundaryTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        ChartDataPoint point = new ChartDataPoint();
        point.setConfigId(100L);
        point.setXValue("2024");
        point.setYValue(123.0);
        point.setLabel("Test Label");
        point.setMetadata("{\"test\": \"metadata\"}");
        point.setTimestamp(boundaryTime);
        entityManager.persist(point);
        entityManager.flush();
        entityManager.clear();
        List<ChartDataPoint> result = chartDataPointRepository.findByConfigIdAndTimestampBetween(100L, boundaryTime, boundaryTime);
        assertEquals(1, result.size());
    }

    @Test
    void findByConfigIdAndTimestampBetween_withMultipleConfigs_shouldOnlyReturnRequestedConfig() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        ChartDataPoint dataPoint1 = createChartDataPoint(1L, "2023", 100.0, LocalDateTime.now());
        ChartDataPoint dataPoint2 = createChartDataPoint(2L, "2024", 200.0, LocalDateTime.now());

        // When
        List<ChartDataPoint> result = chartDataPointRepository.findByConfigIdAndTimestampBetween(1L, startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigId()).isEqualTo(1L);
        assertThat(result.get(0).getYValue()).isEqualTo(100.0);
    }

    @Test
    void findByConfigIdOrderByTimestamp_shouldReturnOrderedByTimestamp() {
        // Given
        LocalDateTime time1 = LocalDateTime.now().minusDays(2);
        LocalDateTime time2 = LocalDateTime.now().minusDays(1);
        LocalDateTime time3 = LocalDateTime.now();
        
        ChartDataPoint dataPoint3 = createChartDataPoint(1L, "2024", 300.0, time3);
        ChartDataPoint dataPoint1 = createChartDataPoint(1L, "2022", 100.0, time1);
        ChartDataPoint dataPoint2 = createChartDataPoint(1L, "2023", 200.0, time2);

        // When
        List<ChartDataPoint> result = chartDataPointRepository.findByConfigIdOrderByTimestamp(1L);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("yValue").containsExactly(100.0, 200.0, 300.0);
    }

    private ChartDataPoint createChartDataPoint(Long configId, String xValue, Double yValue, LocalDateTime timestamp) {
        ChartDataPoint dataPoint = new ChartDataPoint();
        dataPoint.setConfigId(configId);
        dataPoint.setXValue(xValue);
        dataPoint.setYValue(yValue);
        dataPoint.setLabel("Test Label");
        dataPoint.setMetadata("{\"test\": \"metadata\"}");
        dataPoint.setTimestamp(timestamp);
        return entityManager.persistAndFlush(dataPoint);
    }
} 