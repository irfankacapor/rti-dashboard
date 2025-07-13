package io.dashboard.service;

import io.dashboard.dto.SubareaCreateRequest;
import io.dashboard.dto.SubareaResponse;
import io.dashboard.dto.SubareaUpdateRequest;
import io.dashboard.dto.IndicatorValuesResponse;
import io.dashboard.dto.SubareaDataResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Area;
import io.dashboard.model.Subarea;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SubareaServiceTest {
    @Mock
    private SubareaRepository subareaRepository;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private FactIndicatorValueRepository factIndicatorValueRepository;
    @Mock
    private AggregationService aggregationService;
    @Mock
    private IndicatorService indicatorService;
    
    @InjectMocks
    private SubareaService subareaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        subarea.setName("Test Subarea");
        subarea.setCode("S1" + System.nanoTime()); // ensure unique code
        List<Subarea> subareas = List.of(subarea);
        when(subareaRepository.findAll()).thenReturn(subareas);
    }

    @Test
    void findAll_returnsAllSubareas() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        subarea.setName("Test Subarea");
        subarea.setCode("S1" + System.nanoTime()); // ensure unique code
        when(subareaRepository.findAll()).thenReturn(List.of(subarea));
        List<SubareaResponse> result = subareaService.findAll();
        assertEquals(1, result.size());
        assertEquals("Test Subarea", result.get(0).getName());
    }

    @Test
    void findAll_throwsException_whenRepositoryFails() {
        when(subareaRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        assertThatThrownBy(() -> subareaService.findAll())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch subareas");
    }

    @Test
    void findByAreaId_returnsSubareasForArea() {
        Subarea sub = new Subarea();
        sub.setId(2L);
        sub.setCode("S2");
        when(subareaRepository.findByAreaId(10L)).thenReturn(List.of(sub));
        List<SubareaResponse> result = subareaService.findByAreaId(10L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void findById_found() {
        Subarea sub = new Subarea();
        sub.setId(3L);
        sub.setCode("S3");
        when(subareaRepository.findById(3L)).thenReturn(Optional.of(sub));
        SubareaResponse resp = subareaService.findById(3L);
        assertNotNull(resp);
        assertEquals("S3", resp.getCode());
    }

    @Test
    void findById_notFound() {
        when(subareaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById_returnsTrue() {
        when(subareaRepository.existsById(1L)).thenReturn(true);
        assertThat(subareaService.existsById(1L)).isTrue();
    }

    @Test
    void existsById_returnsFalse() {
        when(subareaRepository.existsById(1L)).thenReturn(false);
        assertThat(subareaService.existsById(1L)).isFalse();
    }

    @Test
    void create_success() {
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("S4");
        req.setName("Subarea 4");
        req.setAreaId(1L);
        Area area = new Area();
        area.setId(1L);
        when(subareaRepository.existsByCode("S4")).thenReturn(false);
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(subareaRepository.save(any(Subarea.class))).thenAnswer(inv -> {
            Subarea s = inv.getArgument(0);
            s.setId(44L);
            return s;
        });
        SubareaResponse resp = subareaService.create(req);
        assertThat(resp.getId()).isEqualTo(44L);
        assertThat(resp.getCode()).isEqualTo("S4");
    }

    @Test
    void create_duplicateCode() {
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("DUP");
        when(subareaRepository.existsByCode("DUP")).thenReturn(true);
        assertThatThrownBy(() -> subareaService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unique");
    }

    @Test
    void create_invalidArea() {
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("S5");
        req.setAreaId(123L);
        when(subareaRepository.existsByCode("S5")).thenReturn(false);
        when(areaRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Area does not exist");
    }

    @Test
    void update_success() {
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setName("Updated");
        req.setAreaId(2L);
        Area area = new Area();
        area.setId(2L);
        Subarea sub = new Subarea();
        sub.setId(5L);
        when(subareaRepository.findById(5L)).thenReturn(Optional.of(sub));
        when(areaRepository.findById(2L)).thenReturn(Optional.of(area));
        when(subareaRepository.save(any(Subarea.class))).thenAnswer(inv -> inv.getArgument(0));
        SubareaResponse resp = subareaService.update(5L, req);
        assertThat(resp.getName()).isEqualTo("Updated");
        assertThat(resp.getAreaId()).isEqualTo(2L);
    }

    @Test
    void update_notFound() {
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setAreaId(1L);
        when(subareaRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.update(77L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_invalidArea() {
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setAreaId(8L);
        Subarea sub = new Subarea();
        sub.setId(6L);
        when(subareaRepository.findById(6L)).thenReturn(Optional.of(sub));
        when(areaRepository.findById(8L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.update(6L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Area does not exist");
    }

    @Test
    void delete_success() {
        Subarea sub = new Subarea();
        sub.setId(7L);
        when(subareaRepository.findById(7L)).thenReturn(Optional.of(sub));
        when(factIndicatorValueRepository.findBySubareaId(7L)).thenReturn(Collections.emptyList());
        subareaService.delete(7L);
        verify(subareaRepository).delete(sub);
    }

    @Test
    void delete_withIndicators() {
        Subarea sub = new Subarea();
        sub.setId(8L);
        when(subareaRepository.findById(8L)).thenReturn(Optional.of(sub));
        when(factIndicatorValueRepository.findBySubareaId(8L)).thenReturn(List.of(new FactIndicatorValue()));
        assertThatThrownBy(() -> subareaService.delete(8L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("has associated data");
    }

    @Test
    void delete_notFound() {
        when(subareaRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteWithData_success() {
        Subarea sub = new Subarea();
        sub.setId(9L);
        FactIndicatorValue fact = new FactIndicatorValue();
        when(subareaRepository.findById(9L)).thenReturn(Optional.of(sub));
        when(factIndicatorValueRepository.findBySubareaId(9L)).thenReturn(List.of(fact));
        subareaService.deleteWithData(9L);
        verify(factIndicatorValueRepository).deleteAll(List.of(fact));
        verify(subareaRepository).delete(sub);
    }

    @Test
    void deleteWithData_notFound() {
        when(subareaRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.deleteWithData(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void calculateAggregatedValue_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        double result = subareaService.calculateAggregatedValue(1L);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void calculateAggregatedValue_subareaNotFound() {
        when(subareaRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> subareaService.calculateAggregatedValue(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void calculateAggregatedValue_throwsException() {
        // The current implementation just returns 0.0 and doesn't call aggregation service
        // This test should be updated to test a scenario where an exception could actually occur
        when(subareaRepository.existsById(1L)).thenThrow(new RuntimeException("Database error"));
        assertThatThrownBy(() -> subareaService.calculateAggregatedValue(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to calculate aggregated value for subarea");
    }

    @Test
    void getAggregatedByTime_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(aggregationService.getSubareaAggregatedByTime(1L)).thenReturn(new HashMap<>());
        Map<String, Double> result = subareaService.getAggregatedByTime(1L);
        assertThat(result).isNotNull();
    }

    @Test
    void getAggregatedByTime_subareaNotFound() {
        when(subareaRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> subareaService.getAggregatedByTime(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAggregatedByLocation_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(aggregationService.getSubareaAggregatedByLocation(1L)).thenReturn(new HashMap<>());
        Map<String, Double> result = subareaService.getAggregatedByLocation(1L);
        assertThat(result).isNotNull();
    }

    @Test
    void getAggregatedByLocation_subareaNotFound() {
        when(subareaRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> subareaService.getAggregatedByLocation(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAggregatedByDimension_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(aggregationService.getSubareaAggregatedByDimension(1L, "time")).thenReturn(new HashMap<>());
        Map<String, Double> result = subareaService.getAggregatedByDimension(1L, "time");
        assertThat(result).isNotNull();
    }

    @Test
    void getAggregatedByDimension_subareaNotFound() {
        when(subareaRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> subareaService.getAggregatedByDimension(1L, "time"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAggregatedByDimension_throwsException() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(aggregationService.getSubareaAggregatedByDimension(1L, "time")).thenThrow(new RuntimeException("Failed"));
        assertThatThrownBy(() -> subareaService.getAggregatedByDimension(1L, "time"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getIndicatorValuesForSubarea_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(factIndicatorValueRepository.findByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(List.of());
        List<FactIndicatorValue> result = subareaService.getIndicatorValuesForSubarea(1L, 1L);
        assertThat(result).isNotNull();
    }

    @Test
    void getIndicatorAggregatedValueForSubarea_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(factIndicatorValueRepository.findByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(List.of());
        double result = subareaService.getIndicatorAggregatedValueForSubarea(1L, 1L);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void getIndicatorDimensionsForSubarea_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(factIndicatorValueRepository.findByIndicatorIdAndSubareaId(1L, 1L)).thenReturn(List.of());
        List<String> result = subareaService.getIndicatorDimensionsForSubarea(1L, 1L);
        assertThat(result).isNotNull();
    }

    @Test
    void getIndicatorValuesResponseForSubarea_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(indicatorService.getIndicatorValues(1L)).thenReturn(new IndicatorValuesResponse());
        IndicatorValuesResponse result = subareaService.getIndicatorValuesResponseForSubarea(1L, 1L);
        assertThat(result).isNotNull();
    }

    @Test
    void getSubareaData_success() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        subarea.setName("Test Subarea");
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(factIndicatorValueRepository.findBySubareaIdWithEagerLoading(1L)).thenReturn(List.of());
        SubareaDataResponse result = subareaService.getSubareaData(1L);
        assertThat(result).isNotNull();
    }

    @Test
    void getSubareaData_notFound() {
        when(subareaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.getSubareaData(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
} 