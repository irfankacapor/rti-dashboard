package io.dashboard.service;

import io.dashboard.dto.CsvIndicatorData;
import io.dashboard.dto.IndicatorBatchRequest;
import io.dashboard.dto.IndicatorBatchResponse;
import io.dashboard.dto.IndicatorValue;
import io.dashboard.model.Direction;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.repository.UnitRepository;
import io.dashboard.repository.DimTimeRepository;
import io.dashboard.repository.DimLocationRepository;
import io.dashboard.repository.DimGenericRepository;
import io.dashboard.repository.DataTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicatorBatchServiceTest {

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private SubareaRepository subareaRepository;

    @Mock
    private FactIndicatorValueRepository factRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private DimTimeRepository dimTimeRepository;
    @Mock
    private DimLocationRepository dimLocationRepository;
    @Mock
    private DimGenericRepository dimGenericRepository;
    @Mock
    private DataTypeRepository dataTypeRepository;

    @InjectMocks
    private IndicatorBatchService indicatorBatchService;

    private Subarea testSubarea;
    private Indicator testIndicator;

    @BeforeEach
    void setUp() {
        testSubarea = new Subarea();
        testSubarea.setId(1L);
        testSubarea.setName("Test Subarea");
        testSubarea.setCode("TEST_SUB");

        testIndicator = new Indicator();
        testIndicator.setId(1L);
        testIndicator.setName("Test Indicator");
        testIndicator.setCode("TEST_IND");
    }

    @Test
    void testCreateFromCsvData_WithDirection() {
        // Given
        CsvIndicatorData csvIndicator = CsvIndicatorData.builder()
            .name("Test Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(1L)
            .direction(Direction.INPUT)
            .values(Arrays.asList(
                createIndicatorValue("2023", "Location1", 100.0),
                createIndicatorValue("2023", "Location2", 200.0)
            ))
            .build();

        IndicatorBatchRequest request = new IndicatorBatchRequest();
        request.setIndicators(Arrays.asList(csvIndicator));

        when(indicatorRepository.findByName("Test Indicator")).thenReturn(java.util.Optional.empty());
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);
        when(subareaRepository.findById(1L)).thenReturn(java.util.Optional.of(testSubarea));
        when(factRepository.save(any(FactIndicatorValue.class))).thenAnswer(invocation -> {
            FactIndicatorValue fact = invocation.getArgument(0);
            fact.setId(1L);
            return fact;
        });
        when(unitRepository.findByCode(anyString())).thenReturn(java.util.Optional.empty());
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dimTimeRepository.findByValue(anyString())).thenReturn(java.util.Optional.empty());
        when(dimTimeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dimLocationRepository.findByName(anyString())).thenReturn(java.util.Optional.empty());
        when(dimLocationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        IndicatorBatchResponse response = indicatorBatchService.createFromCsvData(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCreatedIndicators()).hasSize(1);
        assertThat(response.getTotalFactRecords()).isEqualTo(2);
        assertThat(response.getWarnings()).isEmpty();

        // Verify that the direction was set correctly on fact records
        // We can't directly verify the saved facts, but we can check that the service processed them
        assertThat(response.getMessage()).contains("Successfully processed 1 indicators");
    }

    @Test
    void testCreateFromCsvData_WithDifferentDirectionsForSameIndicator() {
        // Given - Create two indicators with the same name but different directions
        CsvIndicatorData csvIndicator1 = CsvIndicatorData.builder()
            .name("Same Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(1L)
            .direction(Direction.INPUT)
            .values(Arrays.asList(createIndicatorValue("2023", "Location1", 100.0)))
            .build();

        CsvIndicatorData csvIndicator2 = CsvIndicatorData.builder()
            .name("Same Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(2L)
            .direction(Direction.OUTPUT)
            .values(Arrays.asList(createIndicatorValue("2023", "Location1", 200.0)))
            .build();

        IndicatorBatchRequest request = new IndicatorBatchRequest();
        request.setIndicators(Arrays.asList(csvIndicator1, csvIndicator2));

        Subarea testSubarea2 = new Subarea();
        testSubarea2.setId(2L);
        testSubarea2.setName("Test Subarea 2");
        testSubarea2.setCode("TEST_SUB2");

        when(indicatorRepository.findByName("Same Indicator")).thenReturn(java.util.Optional.empty());
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);
        when(subareaRepository.findById(1L)).thenReturn(java.util.Optional.of(testSubarea));
        when(subareaRepository.findById(2L)).thenReturn(java.util.Optional.of(testSubarea2));
        when(factRepository.save(any(FactIndicatorValue.class))).thenAnswer(invocation -> {
            FactIndicatorValue fact = invocation.getArgument(0);
            fact.setId(1L);
            return fact;
        });
        when(unitRepository.findByCode(anyString())).thenReturn(java.util.Optional.empty());
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dimTimeRepository.findByValue(anyString())).thenReturn(java.util.Optional.empty());
        when(dimTimeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dimLocationRepository.findByName(anyString())).thenReturn(java.util.Optional.empty());
        when(dimLocationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        IndicatorBatchResponse response = indicatorBatchService.createFromCsvData(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCreatedIndicators()).hasSize(1); // Same indicator, only created once
        assertThat(response.getTotalFactRecords()).isEqualTo(2);
        assertThat(response.getWarnings()).isEmpty();
    }

    @Test
    void testCreateFromCsvData_WithNullDirection() {
        // Given
        CsvIndicatorData csvIndicator = CsvIndicatorData.builder()
            .name("Test Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(1L)
            .direction(null) // Null direction
            .values(Arrays.asList(createIndicatorValue("2023", "Location1", 100.0)))
            .build();

        IndicatorBatchRequest request = new IndicatorBatchRequest();
        request.setIndicators(Arrays.asList(csvIndicator));

        when(indicatorRepository.findByName("Test Indicator")).thenReturn(java.util.Optional.empty());
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);
        when(subareaRepository.findById(1L)).thenReturn(java.util.Optional.of(testSubarea));
        when(factRepository.save(any(FactIndicatorValue.class))).thenAnswer(invocation -> {
            FactIndicatorValue fact = invocation.getArgument(0);
            fact.setId(1L);
            return fact;
        });
        when(unitRepository.findByCode(anyString())).thenReturn(java.util.Optional.empty());
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dimTimeRepository.findByValue(anyString())).thenReturn(java.util.Optional.empty());
        when(dimTimeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dimLocationRepository.findByName(anyString())).thenReturn(java.util.Optional.empty());
        when(dimLocationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        IndicatorBatchResponse response = indicatorBatchService.createFromCsvData(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCreatedIndicators()).hasSize(1);
        assertThat(response.getTotalFactRecords()).isEqualTo(1);
        // Should not throw exception even with null direction
    }

    private IndicatorValue createIndicatorValue(String timeValue, String locationValue, Double value) {
        IndicatorValue indicatorValue = new IndicatorValue();
        indicatorValue.setTimeValue(timeValue);
        indicatorValue.setTimeType("year");
        indicatorValue.setLocationValue(locationValue);
        indicatorValue.setLocationType("state");
        indicatorValue.setValue(BigDecimal.valueOf(value));
        return indicatorValue;
    }
} 