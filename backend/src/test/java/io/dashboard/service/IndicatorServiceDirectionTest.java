package io.dashboard.service;

import io.dashboard.dto.IndicatorResponse;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicatorServiceDirectionTest {

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private FactIndicatorValueRepository factIndicatorValueRepository;

    @InjectMocks
    private IndicatorService indicatorService;

    private Indicator testIndicator;
    private Subarea testSubarea1;
    private Subarea testSubarea2;

    @BeforeEach
    void setUp() {
        testIndicator = new Indicator();
        testIndicator.setId(1L);
        testIndicator.setName("Test Indicator");
        testIndicator.setCode("TEST_IND");

        testSubarea1 = new Subarea();
        testSubarea1.setId(1L);
        testSubarea1.setName("Subarea 1");

        testSubarea2 = new Subarea();
        testSubarea2.setId(2L);
        testSubarea2.setName("Subarea 2");
    }

    @Test
    void testFindByFactSubareaId_WithInputDirection() {
        // Given
        List<FactIndicatorValue> facts = Arrays.asList(
            createFactIndicatorValue("input"),
            createFactIndicatorValue("input"),
            createFactIndicatorValue("input")
        );

        when(indicatorRepository.findByFactSubareaId(1L)).thenReturn(Arrays.asList(testIndicator));
        when(factIndicatorValueRepository.countByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(3L);
        when(factIndicatorValueRepository.findDimensionsByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(Arrays.asList("time", "location"));
        when(factIndicatorValueRepository.findByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(facts);

        // When
        List<IndicatorResponse> responses = indicatorService.findByFactSubareaId(1L);

        // Then
        assertThat(responses).hasSize(1);
        IndicatorResponse response = responses.get(0);
        assertThat(response.getDirection()).isEqualTo("input");
        assertThat(response.getValueCount()).isEqualTo(3L);
    }

    @Test
    void testFindByFactSubareaId_WithOutputDirection() {
        // Given
        List<FactIndicatorValue> facts = Arrays.asList(
            createFactIndicatorValue("output"),
            createFactIndicatorValue("output"),
            createFactIndicatorValue("output")
        );

        when(indicatorRepository.findByFactSubareaId(1L)).thenReturn(Arrays.asList(testIndicator));
        when(factIndicatorValueRepository.countByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(3L);
        when(factIndicatorValueRepository.findDimensionsByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(Arrays.asList("time", "location"));
        when(factIndicatorValueRepository.findByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(facts);

        // When
        List<IndicatorResponse> responses = indicatorService.findByFactSubareaId(1L);

        // Then
        assertThat(responses).hasSize(1);
        IndicatorResponse response = responses.get(0);
        assertThat(response.getDirection()).isEqualTo("output");
        assertThat(response.getValueCount()).isEqualTo(3L);
    }

    @Test
    void testFindByFactSubareaId_WithMixedDirections_ShouldReturnMostCommon() {
        // Given - More input than output
        List<FactIndicatorValue> facts = Arrays.asList(
            createFactIndicatorValue("input"),
            createFactIndicatorValue("input"),
            createFactIndicatorValue("input"),
            createFactIndicatorValue("output"),
            createFactIndicatorValue("output")
        );

        when(indicatorRepository.findByFactSubareaId(1L)).thenReturn(Arrays.asList(testIndicator));
        when(factIndicatorValueRepository.countByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(5L);
        when(factIndicatorValueRepository.findDimensionsByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(Arrays.asList("time", "location"));
        when(factIndicatorValueRepository.findByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(facts);

        // When
        List<IndicatorResponse> responses = indicatorService.findByFactSubareaId(1L);

        // Then
        assertThat(responses).hasSize(1);
        IndicatorResponse response = responses.get(0);
        assertThat(response.getDirection()).isEqualTo("input"); // Most common
        assertThat(response.getValueCount()).isEqualTo(5L);
    }

    @Test
    void testFindByFactSubareaId_WithNullDirections() {
        // Given
        List<FactIndicatorValue> facts = Arrays.asList(
            createFactIndicatorValue(null),
            createFactIndicatorValue(null),
            createFactIndicatorValue(null)
        );

        when(indicatorRepository.findByFactSubareaId(1L)).thenReturn(Arrays.asList(testIndicator));
        when(factIndicatorValueRepository.countByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(3L);
        when(factIndicatorValueRepository.findDimensionsByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(Arrays.asList("time", "location"));
        when(factIndicatorValueRepository.findByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(facts);

        // When
        List<IndicatorResponse> responses = indicatorService.findByFactSubareaId(1L);

        // Then
        assertThat(responses).hasSize(1);
        IndicatorResponse response = responses.get(0);
        assertThat(response.getDirection()).isNull();
        assertThat(response.getValueCount()).isEqualTo(3L);
    }

    @Test
    void testFindByFactSubareaId_WithMixedNullAndValidDirections() {
        // Given - Some null, some valid directions
        List<FactIndicatorValue> facts = Arrays.asList(
            createFactIndicatorValue(null),
            createFactIndicatorValue("input"),
            createFactIndicatorValue("input"),
            createFactIndicatorValue(null)
        );

        when(indicatorRepository.findByFactSubareaId(1L)).thenReturn(Arrays.asList(testIndicator));
        when(factIndicatorValueRepository.countByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(4L);
        when(factIndicatorValueRepository.findDimensionsByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(Arrays.asList("time", "location"));
        when(factIndicatorValueRepository.findByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(facts);

        // When
        List<IndicatorResponse> responses = indicatorService.findByFactSubareaId(1L);

        // Then
        assertThat(responses).hasSize(1);
        IndicatorResponse response = responses.get(0);
        assertThat(response.getDirection()).isEqualTo("input"); // Should return the valid direction
        assertThat(response.getValueCount()).isEqualTo(4L);
    }

    @Test
    void testFindAll_WithMixedDirectionsAcrossSubareas() {
        // Given - Same indicator with different directions in different subareas
        List<FactIndicatorValue> allFacts = Arrays.asList(
            createFactIndicatorValue("input", testSubarea1),
            createFactIndicatorValue("input", testSubarea1),
            createFactIndicatorValue("output", testSubarea2),
            createFactIndicatorValue("output", testSubarea2),
            createFactIndicatorValue("output", testSubarea2)
        );

        when(indicatorRepository.findAll()).thenReturn(Arrays.asList(testIndicator));
        when(factIndicatorValueRepository.countByIndicatorId(1L)).thenReturn(5L);
        when(factIndicatorValueRepository.findDimensionsByIndicatorId(1L)).thenReturn(Arrays.asList("time", "location"));
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(allFacts);

        // When
        List<IndicatorResponse> responses = indicatorService.findAll();

        // Then
        assertThat(responses).hasSize(1);
        IndicatorResponse response = responses.get(0);
        assertThat(response.getDirection()).isEqualTo("output"); // Most common across all subareas
        assertThat(response.getValueCount()).isEqualTo(5L);
    }

    private FactIndicatorValue createFactIndicatorValue(String direction) {
        return createFactIndicatorValue(direction, testSubarea1);
    }

    private FactIndicatorValue createFactIndicatorValue(String direction, Subarea subarea) {
        FactIndicatorValue fact = new FactIndicatorValue();
        fact.setId(1L);
        fact.setIndicator(testIndicator);
        fact.setSubarea(subarea);
        fact.setValue(BigDecimal.valueOf(100.0));
        fact.setDirection(direction);
        return fact;
    }
} 